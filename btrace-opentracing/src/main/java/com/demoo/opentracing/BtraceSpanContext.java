package com.demoo.opentracing;

import io.opentracing.SpanContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhxy
 * @Date 2021/6/27 2:52 下午
 */
public class BtraceSpanContext implements SpanContext {

    protected static final byte flagSampled = 1;
    protected static final byte flagDebug = 2;

    private String traceId;
    private String spanId;
    private String parentId;
    private byte flags;
    private Map<String, String> baggages;

    @Override
    public String toTraceId() {
        return traceId;
    }

    @Override
    public String toSpanId() {
        return spanId;
    }


    public static BtraceSpanContext buildChildSpanContext(BtraceSpanContext parentBtraceSpanContext, BtraceTracer tracer) {
        return new BtraceSpanContext(
                parentBtraceSpanContext.getTraceId(),
                tracer.getSpanIdGenerator().getId(),
                parentBtraceSpanContext.getSpanId(),
                parentBtraceSpanContext.getFlags(),
                parentBtraceSpanContext.getBaggages());
    }

    public BtraceSpanContext(String traceId, String spanId, String parentId, byte flags) {
        this(traceId, spanId, parentId, flags, new HashMap<>(0));
    }

    public BtraceSpanContext(String traceId, String spanId, String parentId, byte flags, Map<String, String> baggages) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = parentId;
        this.flags = flags;
        this.baggages = baggages != null ? baggages : new HashMap<String, String>(0);
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        Map<String, String> baggageMap = new HashMap<String, String>(baggages);
        return baggageMap.entrySet();
    }


    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getParentId() {
        return parentId;
    }

    public byte getFlags() {
        return flags;
    }

    public Map<String, String> getBaggages() {
        return baggages;
    }

    public boolean isSampled() {
        return (flags & flagSampled) == flagSampled;
    }

    public boolean isDebug() {
        return (flags & flagDebug) == flagDebug;
    }

    public String contextAsString() {
        return String.format("tid:%s sid:%s flag:%d", traceId, spanId, flags);
    }

    @Override
    public String toString() {
        return contextAsString();
    }
}
