package com.demoo.btrace.flink.utils;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author zhxy
 * @Date 2021/7/1 6:16 下午
 */

@Slf4j
public class DateUtil {
    private static final String YMDHMSS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    // thread safe
    private static final DateTimeFormatter formatter_yyyMMddHHmmssSSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
    private static final DateTimeFormatter formatter_yyyMMddHHmmssSSS_other = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss:SSS");
    private static final DateTimeFormatter formatter_yyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 计算0点的毫秒数
    public static Long midnight(long millis) {
        Calendar calendar = midnightCalendar(millis);
        return calendar.getTimeInMillis();
    }

    //计算下一个0点的毫秒数
    public static long nextMidnight(long millis) {
        Calendar calendar = midnightCalendar(millis);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTimeInMillis();
    }

    public static Calendar midnightCalendar(Long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar;
    }

    /**
     * 获取小时上限毫秒
     *
     * @param millis
     * @return
     */
    public static Long highLimitMills(Long millis) {
        Calendar calendar = hourCalendar(millis);
        calendar.add(Calendar.HOUR, 1);
        return calendar.getTimeInMillis();
    }


    /**
     * 获取指定时间戳区间里的小时字符串
     *
     * @param startMillis
     * @param endMillis
     * @return
     */
    public static List<String> getHourListByRangeTime(Long startMillis, Long endMillis) {
        List<String> hourList = Lists.newArrayList();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
        String endDate = dateFormat.format(endMillis);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startMillis);

        String curDate = "";
        do {
            curDate = dateFormat.format(calendar.getTime());
            hourList.add(curDate);
            calendar.add(Calendar.HOUR, 1);
        } while (!curDate.equals(endDate));
        System.out.println(hourList.toString());
        return hourList;
    }

    /**
     * 获取小时下限毫秒
     *
     * @param millis
     * @return
     */
    public static Long lowerLimitMills(Long millis) {
        Calendar calendar = hourCalendar(millis);
        return calendar.getTimeInMillis();
    }


    public static Calendar hourCalendar(Long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar;
    }

    /**
     * 得到millis所在周的周一的零点
     *
     * @param millis
     * @return
     */
    public static long getMondayZeroMillis(long millis) {
        Calendar cal = midnightCalendar(millis);
        int d = 0;
        int weekDay = cal.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1) {
            d = -6;
        } else {
            d = 2 - weekDay;
        }
        cal.add(Calendar.DAY_OF_WEEK, d);
        return cal.getTimeInMillis();
    }

    /**
     * 得到millis所在周的,下一周的,周一的零点
     *
     * @param millis
     * @return
     */
    public static long getSundayEndMillis(long millis) {
        long mondayZeroMillis = getMondayZeroMillis(millis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mondayZeroMillis);
        calendar.add(Calendar.DAY_OF_WEEK, 7);
        return calendar.getTimeInMillis();
    }


    public static String getTimeByGivenFormat(long millis, String format) {
        Date date = new Date(millis);
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.format(date);
    }


    public static Long logCenterymdhmssToTimestamp(String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return null;
        }
        SimpleDateFormat sf = new SimpleDateFormat(YMDHMSS);
        sf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = sf.parse(dateString);
        } catch (ParseException e) {
            log.error("日期解析成date错误:{}", dateString);
        }
        return date == null ? null : date.getTime();
    }

    /**
     * format localDate
     *
     * @param localDate
     * @param pattern   yyyy-MM-dd、yyyy-MM等
     * @return
     */
    public static String format(LocalDate localDate, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDate.format(formatter);
    }

    // 格式化时间
    public static Date parse(String dateStr, String pattern) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(pattern);
        return sf.parse(dateStr);
    }

    /**
     * 根据时间字符串获取对应毫秒数据
     *
     * @param dateStr
     * @return
     */
    public static Long getTimeMillisByDateStr(String dateStr) {
        if (dateStr.length() == 23) {
            if (dateStr.indexOf(":") == 4) {
                return LocalDateTime.parse(dateStr, formatter_yyyMMddHHmmssSSS_other).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            return LocalDateTime.parse(dateStr, formatter_yyyMMddHHmmssSSS).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else if (dateStr.length() == 19) {
            return LocalDateTime.parse(dateStr, formatter_yyyMMddHHmmss).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return System.currentTimeMillis();
        }
    }

    /**
     * 获取特定时间点的字符串 向后推或者向前推一定时间段
     *
     * @param minutes
     * @param format
     * @return
     */
    public static String getSpecificDateStr(int type, int minutes, String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(type, minutes);
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.format(calendar.getTime());
    }

    /**
     * 将LocalDate转换成Date
     *
     * @param localDate
     * @return
     */
    public static Date convertLocalDate2Date(LocalDate localDate) {
        ZoneId zoneId = ZoneId.systemDefault();
        Date date = Date.from(localDate.atStartOfDay(zoneId).toInstant());
        return date;
    }

}
