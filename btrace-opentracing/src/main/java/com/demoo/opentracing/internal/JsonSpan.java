package com.demoo.opentracing.internal;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
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

    @Data
    public static class Log implements Serializable {
        private long ts;
        private Map<String, String> fields = new HashMap<>();

        @Override
        public String toString() {
            return "Log{" +
                    "ts=" + ts +
                    ", fields=" + fields +
                    '}';
        }
    }
}