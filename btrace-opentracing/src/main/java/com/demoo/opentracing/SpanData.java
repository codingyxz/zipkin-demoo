package com.demoo.opentracing;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * span数据包装类
 * @author zhxy
 * @Date 2021/7/1 5:01 下午
 */
@Data
@Accessors(chain = true)
public class SpanData implements Serializable {

    private static final long serialVersionUID = -2477905515450412220L;
    private String parentId;
    private String traceId;
    private String id;
    private String kind;
    private String name;
    private long timestamp;
    private String reference;
    private long duration;
    private Map<String, Object> tags;
    private List<Log> logs;
    private Map<String, String> baggages;
    private EndPoint endPoint;

    public SpanData() {
        tags = new HashMap<String, Object>();
        logs = new ArrayList<Log>();
        baggages = new HashMap<String, String>();

    }

    public static SpanData build(BtraceSpanContext btraceSpanContext) {
        SpanData spanData = new SpanData();
        spanData.setId(btraceSpanContext.getSpanId());
        spanData.setParentId(btraceSpanContext.getParentId());
        spanData.setTraceId(btraceSpanContext.getTraceId());
        spanData.setBaggages(btraceSpanContext.getBaggages());
        return spanData;
    }

    @ToString
    @Data
    @Accessors(chain = true)
    public static class Log implements Serializable {
        private static final long serialVersionUID = -3925299552213674892L;
        private long timestamp;
        private Map<String, String> fields = new HashMap<>();

        public static Log create(long timestamp, Map<String, String> fields) {
            Log log = new Log();
            log.setTimestamp(timestamp);
            log.setFields(fields);
            return log;
        }
    }

    @ToString
    @Accessors(chain = true)
    @Data
    public static class EndPoint implements Serializable {
        private static final long serialVersionUID = 1227855117037061129L;
        private String serviceName;
        private String ip;

        public static EndPoint build(String serviceName, String ip) {
            EndPoint endPoint = new EndPoint();
            endPoint.setIp(ip);
            endPoint.setServiceName(serviceName);
            return endPoint;
        }
    }

    @Override
    public String toString() {
        return "SpanData{" +
                "parentId='" + parentId + '\'' +
                ", traceId='" + traceId + '\'' +
                ", id='" + id + '\'' +
                ", kind='" + kind + '\'' +
                ", name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", reference='" + reference + '\'' +
                ", duration=" + duration +
                ", tags=" + tags +
                ", logs=" + logs +
                ", baggages=" + baggages +
                ", endPoint=" + endPoint +
                '}';
    }
}
