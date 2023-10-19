package com.demoo.opentracing.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JsonSpan implements Serializable {

    private String parentId;
    private String id;
    private String traceId;
    private String name;
    private long duration;
    private String serverName;
    private long timestamp;
    private int ip;

    private Map<String, Object> tags;
    private Map<String, String> baggages;
    private List<Log> logs;


    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getIp() {
        return ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public Map<String, String> getBaggages() {
        return baggages;
    }

    public void setBaggages(Map<String, String> baggages) {
        this.baggages = baggages;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    public static class Log implements Serializable {
        private long ts;
        private Map<String, String> fields = new HashMap<>();

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }

        public Map<String, String> getFields() {
            return fields;
        }

        public void setFields(Map<String, String> fields) {
            this.fields = fields;
        }

        @Override
        public String toString() {
            return "Log{" +
                    "ts=" + ts +
                    ", fields=" + fields +
                    '}';
        }
    }
}