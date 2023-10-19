package com.demoo.opentracing;

import com.demoo.opentracing.idgenerators.IdGenerator;
import com.demoo.opentracing.idgenerators.ZipKinTraceId128BitIdGenerator;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tag;

import java.util.HashMap;
import java.util.Map;


/**
 *
 *
 * @author zhxy
 * @Date 2021/6/27 2:51 下午
 */
public class BtraceSpanBuilder implements Tracer.SpanBuilder {

    private String operationName ;
    private long startTimeMicroseconds;
    private Reference reference ;
    private Map<String, Object> tags;
    private boolean ignoreActiveSpan = false;
    private BtraceTracer tracer;

    public BtraceSpanBuilder(BtraceTracer tracer, String operationName) {
        this.operationName = operationName;
        this.tracer = tracer;
        tags = new HashMap<>();
    }

    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext parent) {
        return addReference(References.CHILD_OF, parent);
    }

    @Override
    public Tracer.SpanBuilder asChildOf(Span parent) {
        return addReference(References.CHILD_OF, parent.context());
    }

    @Override
    public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
        if (!References.CHILD_OF.equals(referenceType)
                && !References.FOLLOWS_FROM.equals(referenceType)) {
            return this;
        }
        reference = new Reference(referencedContext, referenceType);
        return this;
    }

    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() {
        ignoreActiveSpan = true;
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public <T> Tracer.SpanBuilder withTag(Tag<T> tag, T value) {
//        tags.put(key, value);
        return this;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        startTimeMicroseconds = microseconds;
        return this;
    }

    @Override
    public Span start() {

        // Check if active span should be established as CHILD_OF relationship
        if (reference == null && !ignoreActiveSpan && tracer.activeSpan() != null) {
            asChildOf(tracer.activeSpan());
        }
        BtraceSpanContext context = reference == null ? createNewContext() : createChildContext();
        if(startTimeMicroseconds == 0){
            startTimeMicroseconds = tracer.getClock().currentTimeMicros();
        }

        return new BtraceSpan(tracer,operationName,context,startTimeMicroseconds,tags,reference);

    }

    /**
     * 创建新的 span context
     * @return
     */
    private BtraceSpanContext createNewContext() {
        String traceId = tracer.getTraceIdGenerator().getId();
        // 兼容zipkin parent id 的逻辑，zipkin parent id 只能是64bit
        String parentId = (tracer.getTraceIdGenerator() instanceof ZipKinTraceId128BitIdGenerator) ?
                traceId.substring(16) : traceId;
        String spanId = tracer.getSpanIdGenerator().getId();

        byte flags = 0;
        if(tracer.getSampler().isSampled(traceId)){
            flags |= BtraceSpanContext.flagSampled;
        }
        return new BtraceSpanContext(traceId,spanId,parentId,flags);
    }

    /**
     * 创建child context
     *
     * @return
     */
    private BtraceSpanContext createChildContext(){
        return BtraceSpanContext.buildChildSpanContext((BtraceSpanContext) reference.getSpanContext(),tracer);
    }
}
