package com.demoo.btrace.flink.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author zhxy
 * @Date 2021/7/1 6:14 下午
 */
public class IndexNameFormatter implements Serializable{

    private static String TIME_FORMAT = "yyyy-MM-dd";
    private String indexNamePrefix;
    // 缓存当天的index
    private volatile IndexWithTime cachedIndex = new IndexWithTime();

    private IndexNameFormatter() {
    }

    public String getCachedIndex(Long mills) {
        if (mills == null) {
            return null;
        }
        // double check
        if (!cachedIndex.containTime(mills)) {
            synchronized (this) {
                if (!cachedIndex.containTime(mills)) {
                    String index = getIndex(mills);
                    if (StringUtils.isBlank(index)) {
                        return null;
                    }
                    cachedIndex.set(DateUtil.midnight(mills), DateUtil.nextMidnight(mills), index);
                    return index;
                }
            }
        }
        return cachedIndex.getIndex();
    }

    /**
     * 按周建索引,比如：realtime:frontend:exception-2019-03-04-10
     * @param mills
     * @return
     */
    public String getWeekCachedIndex(Long mills) {
        if (mills == null) {
            return null;
        }
        if (!cachedIndex.containTime(mills)) {
            synchronized (this) {
                if (!cachedIndex.containTime(mills)) {
                    long mondayZeroMillis = DateUtil.getMondayZeroMillis(mills);
                    long sundayEndMillis = DateUtil.getSundayEndMillis(mills);
                    String index = getWeekIndex(mondayZeroMillis);
                    if (StringUtils.isBlank(index)) {
                        return null;
                    }
                    cachedIndex.set(mondayZeroMillis, sundayEndMillis, index);
                    return index;
                }
            }
        }
        return cachedIndex.getIndex();
    }

    public static IndexNameFormatter createIndexNameFormatter(String indexNamePrefix) {
        IndexNameFormatter formatter = new IndexNameFormatter();
        formatter.indexNamePrefix = indexNamePrefix;
        return formatter;
    }

    /**
     * 根据时间区间计算需要查询的index
     *
     * @param beginMillis
     * @param endMills
     * @return
     */
    public List<String> getIndices(Long beginMillis, Long endMills) {
        List<String> indices = new ArrayList<>();
        if (StringUtils.isBlank(indexNamePrefix)) {
            return indices;
        }
        if (beginMillis == null || endMills == null) {
            indices.add(indexNamePrefix + "-*");
            return indices;
        }
        GregorianCalendar calendarBegin = new GregorianCalendar();
        calendarBegin.setTimeInMillis(DateUtil.midnight(beginMillis));
        GregorianCalendar calendarEnd = new GregorianCalendar();
        calendarEnd.setTimeInMillis(endMills);
        while (calendarBegin.compareTo(calendarEnd) <= 0) {
            indices.add(indexNamePrefix + "-" + DateUtil.getTimeByGivenFormat(calendarBegin.getTimeInMillis(), TIME_FORMAT));
            calendarBegin.add(Calendar.DAY_OF_MONTH, 1);
        }
        return indices;
    }

    /**
     * 获取最近2天索引名称数组
     *
     * @return
     */
    public String[] getIndexNameWithRecent2Days() {
        List<String> indices = Lists.newArrayList();
        indices.add(String.format("%s-%s", indexNamePrefix, LocalDate.now().minusDays(1).toString()));
        indices.add(String.format("%s-%s", indexNamePrefix, LocalDate.now().toString()));
        return indices.toArray(new String[0]);
    }

    /**
     * 获取当天索引名称
     *
     * @return
     */
    public String getIndexNameWithCurrentDay() {
        return String.format("%s-%s", indexNamePrefix, LocalDate.now().toString());
    }

    /**
     * 根据毫秒时间获取对应的index名称
     *
     * @param millis
     * @return
     */
    public String getIndex(Long millis) {
        if (StringUtils.isBlank(indexNamePrefix)) {
            return null;
        }
        if (millis == null) {
            return null;
        }
        return indexNamePrefix + "-" + DateUtil.getTimeByGivenFormat(millis, TIME_FORMAT);
    }

    /**
     * 得到mondayZeroMillis所在周的索引
     * @param mondayZeroMillis 周一0点的时间
     * @return
     */
    public String getWeekIndex(Long mondayZeroMillis) {
        if (StringUtils.isBlank(indexNamePrefix)) {
            return null;
        }
        if (mondayZeroMillis == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mondayZeroMillis);
        cal.add(Calendar.DAY_OF_WEEK, 6);
        String mondayStr = DateUtil.getTimeByGivenFormat(mondayZeroMillis, TIME_FORMAT);
        String sundayStr = DateUtil.getTimeByGivenFormat(cal.getTimeInMillis(), "dd");
        return indexNamePrefix + "-" + mondayStr + "-" + sundayStr;
    }

    public List<String> getWeekIndices(Long beginMillis, Long endMills) {
        List<String> indices = new ArrayList<>();
        if (beginMillis == null || endMills == null || endMills < beginMillis) {
            return null;
        }
        if (StringUtils.isBlank(indexNamePrefix)) {
            return indices;
        }
        long mZeroStart = DateUtil.getMondayZeroMillis(beginMillis);
        long mZeroEnd = DateUtil.getMondayZeroMillis(endMills);
        indices.add(getWeekIndex(mZeroStart));
        if(mZeroStart == mZeroEnd){
            return indices;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mZeroStart);
        cal.add(Calendar.DAY_OF_WEEK, 7);
        indices.add(getWeekIndex(cal.getTimeInMillis()));
        while (cal.getTimeInMillis() < mZeroEnd) {
            cal.add(Calendar.DAY_OF_WEEK, 7);
            indices.add(getWeekIndex(cal.getTimeInMillis()));
        }
        return indices;
    }

    /**
     * 获取当前月前缀
     *
     * @return
     */
    public String[] getIndexNamePrefixWithRecent2Months() {
        List<String> indices = Lists.newArrayList();
        indices.add(String.format("%s-%s*", indexNamePrefix, DateUtil.format(LocalDate.now().minusMonths(1), "yyyy-MM")));
        indices.add(String.format("%s-%s*", indexNamePrefix, DateUtil.format(LocalDate.now(), "yyyy-MM")));
        return indices.toArray(new String[0]);
    }

    //用于存放索引名称以及该索引名称对应的时间区间
    static class IndexWithTime implements Serializable {

        private Long startMills;
        private Long endMills;
        private String index;

        public void set(Long startMills, Long endMills, String index) {
            this.startMills = startMills;
            this.endMills = endMills;
            this.index = index;
        }

        public String getIndex() {
            return index;
        }

        public boolean containTime(Long mills) {
            if (startMills == null || endMills == null || this.startMills.compareTo(mills) > 0 || this.endMills.compareTo(mills) <= 0) {
                return false;
            }
            return true;
        }
    }
}
