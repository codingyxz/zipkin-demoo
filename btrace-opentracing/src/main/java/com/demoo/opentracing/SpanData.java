package com.demoo.opentracing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * gtrace span 的数据对象
 * Created by freeway on 2017/9/15.
 */
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public SpanData setLogs(List<Log> logs) {
        this.logs = logs;
        return this;
    }

    public Map<String, String> getBaggages() {
        return baggages;
    }

    public void setBaggages(Map<String, String> baggages) {
        this.baggages = baggages;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

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

        public long getTimestamp() {
            return timestamp;
        }

        public Log setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Map<String, String> getFields() {
            return fields;
        }

        public Log setFields(Map<String, String> fields) {
            this.fields = fields;
            return this;
        }

        @Override
        public String toString() {
            return "Log{" +
                    "timestamp=" + timestamp +
                    ", fields=" + fields +
                    '}';
        }
    }

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

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        @Override
        public String toString() {
            return "EndPoint{" +
                    "serviceName='" + serviceName + '\'' +
                    ", ip='" + ip + '\'' +
                    '}';
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
