package com.demoo.btrace.flink.utils;

import com.demoo.btrace.flink.constants.SpanConstant;
import com.demoo.btrace.flink.domain.BtraceSpan;
import com.demoo.btrace.flink.domain.PbSpan;
import com.demoo.btrace.flink.domain.span.Annotation;
import com.demoo.btrace.flink.domain.span.BinaryAnnotation;
import com.demoo.btrace.flink.domain.span.SpanV1;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhxy
 * @Date 2021/7/1 4:48 下午
 */
public class SpanTransformerUtil {

    private static final String EQUAL = "=";

    /**
     * protobuf span转存储用span
     *
     * @param spans
     * @return
     */
    public static List<BtraceSpan> pbSpanToBtraceSpan(List<PbSpan.Span> spans) {
        List<BtraceSpan> BtraceSpans = new ArrayList<>();
        for (PbSpan.Span span : spans) {
            // 排除掉dubbo监控
            if (SpanConstant.Value.DUBBO_MONITOR_NAME.equals(span.getName())) {
                continue;
            }

            BtraceSpan bSpan = BtraceSpan.buildFromPbSpan(span);

            if (!CollectionUtils.isEmpty(span.getLogsList())) {
                for (PbSpan.Span.Log log : span.getLogsList()) {
                    bSpan.getLogs().add(new BtraceSpan.Log(log.getTimestamp(), log.getKey(), log.getValue()));
                }
            }

            Boolean tagError = false;
            if (!CollectionUtils.isEmpty(span.getTagsList())) {
                for (PbSpan.Span.Pair pair : span.getTagsList()) {
                    // 在tag里如果在链路收集端的检测到有错误则会向tags里增加error=true 若链路没有错误则tags里不会出现error=true的键值对
                    if (SpanConstant.Value.ERROR_KEY.equals(pair.getKey()) && Boolean.parseBoolean(pair.getValue())) {
                        tagError = true;
                    } else {
                        bSpan.getTags().add(new BtraceSpan.Pair(pair.getKey(), pair.getValue()));
                    }
                }
            }

            /**
             * 1.tag的error=true && 不包含 error.object 则链路error=true
             * 2.tag的error=true && 包含 error.object 则链路error=true
             * 3.tag的error=true && 包含 error.object && 通过过滤掉部分 error.object 则链路error=true
             * 4.tag的error=true && 包含 error.object && 过滤掉全部 error.object 则链路error=false   =====>这部分需要特殊处理
             * 5.tag的error=false && 不管error.object是否有 则链路error=false
             */
            if (tagError == false) {
                bSpan.setError(false);
            } else {
                bSpan.setError(true);
                // 链路为错误的时候同时向tag里增加 error=true键值对
                bSpan.getTags().add(new BtraceSpan.Pair(BtraceSpan.Pair.ERROR, "true"));
            }

            if (!CollectionUtils.isEmpty(span.getBaggagesList())) {
                span.getBaggagesList().forEach(a -> {
                    bSpan.getBaggages().add(new BtraceSpan.Pair(a.getKey(), a.getValue()));
                });
            }
            BtraceSpans.add(bSpan);
        }
        return BtraceSpans;
    }

    /**
     * 老款span转新款存储用span
     *
     * @param spanV1s
     * @return
     */
    public static List<BtraceSpan> spanV1ToBtraceSpan(List<SpanV1> spanV1s) {
        List<BtraceSpan> BtraceSpans = new ArrayList<>();
        for (SpanV1 span : spanV1s) {
            BtraceSpan btraceSpan = BtraceSpan.buildFromSpanV1(span);
            annotationsToLogs(btraceSpan, span);
            binaryAnnotationsToTagAndBaggage(btraceSpan, span);
            BtraceSpans.add(btraceSpan);
        }
        return BtraceSpans;
    }


    // annotation转log
    private static void annotationsToLogs(BtraceSpan btraceSpan, SpanV1 spanV1) {
        if (CollectionUtils.isEmpty(spanV1.getAnnotations())) {
            return;
        }
        List<BtraceSpan.Log> logs = new ArrayList<>();
        for (Annotation annotation : spanV1.getAnnotations()) {
            if ((StringUtils.isBlank(btraceSpan.getServiceName()) || StringUtils.isBlank(btraceSpan.getIp())) && annotation.getEndpoint() != null) {
                btraceSpan.setServiceName(annotation.getEndpoint().getServiceName());
                btraceSpan.setIp(IpUtil.getIpByInt(annotation.getEndpoint().getIpv4()));
            }
            BtraceSpan.Log log = BtraceSpan.Log.build(annotation.getTimestamp());
            logs.add(log);
            if (StringUtils.isBlank(annotation.getValue())) {
                continue;
            }
            if (SpanConstant.Value.CLIENT_RECEIVE.equals(annotation.getValue()) || SpanConstant.Value.CLIENT_SEND.equals(annotation.getValue())) {
                log.setKey(SpanConstant.Value.EVENT_KEY);
                log.setValue(annotation.getValue());
                btraceSpan.setKind(SpanConstant.Value.KIND_CLIENT);
            } else if (SpanConstant.Value.SERVER_RECEIVE.equals(annotation.getValue()) || SpanConstant.Value.SERVER_SEND.equals(annotation.getValue())) {
                log.setKey(SpanConstant.Value.EVENT_KEY);
                log.setValue(annotation.getValue());
                btraceSpan.setKind(SpanConstant.Value.KIND_SERVER);
            } else if (annotation.getValue().contains(EQUAL)) {
                int index = annotation.getValue().indexOf(EQUAL);
                log.setKey(annotation.getValue().substring(0, index));
                log.setValue(annotation.getValue().substring(index + 1, annotation.getValue().length()));
            } else {
                log.setKey(SpanConstant.Value.UNKNOWN_KEY);
                log.setValue(annotation.getValue());
            }
        }
        btraceSpan.setLogs(logs);
    }


    // binaryAnnotation 转 tag和logs
    private static void binaryAnnotationsToTagAndBaggage(BtraceSpan btraceSpan, SpanV1 spanV1) {
        if (CollectionUtils.isEmpty(spanV1.getBinaryAnnotations())) {
            return;
        }
        List<BtraceSpan.Pair> tags = new ArrayList<>();
        List<BtraceSpan.Pair> baggages = new ArrayList<>();
        boolean error = false;
        for (BinaryAnnotation binaryAnnotation : spanV1.getBinaryAnnotations()) {
            if ((StringUtils.isBlank(btraceSpan.getServiceName()) || StringUtils.isBlank(btraceSpan.getIp())) && binaryAnnotation.getEndpoint() != null) {
                btraceSpan.setServiceName(binaryAnnotation.getEndpoint().getServiceName());
                btraceSpan.setIp(IpUtil.getIpByInt(binaryAnnotation.getEndpoint().getIpv4()));
            }
            if (binaryAnnotation.getKey().startsWith(SpanConstant.Value.BAGGAGE_PREFIX)) {
                baggages.add(BtraceSpan.Pair.build(binaryAnnotation.getKey().substring(SpanConstant.Value.BAGGAGE_PREFIX.length(), binaryAnnotation.getKey().length()), binaryAnnotation.getValue()));
            } else {
                if (SpanConstant.Value.ERROR_KEY.equals(binaryAnnotation.getKey())) {
                    error = true;
                } else {
                    tags.add(BtraceSpan.Pair.build(binaryAnnotation.getKey(), binaryAnnotation.getValue()));
                }
            }
        }
        btraceSpan.setError(error);
        btraceSpan.setBaggages(baggages);
        btraceSpan.setTags(tags);
    }

    /**
     * 判断链路是否是健康检测
     *
     * @param pair
     * @return
     */
    private static boolean isHealthCheck(PbSpan.Span.Pair pair) {
        if (pair == null) {
            return false;
        }

        if (!SpanConstant.Field.HTTP_URL.equals(pair.getKey())) {
            return false;
        }

        String value = pair.getValue();
        if (StringUtils.isEmpty(value)) {
            return false;
        }

        return true;
    }

}
