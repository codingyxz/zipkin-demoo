package com.demoo.opentracing;

import com.demoo.opentracing.restrictions.Restriction;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhxy
 * @Date 2021/6/27 2:48 下午
 */
public class BtraceSpan implements Span {

    private SpanData spanData;
    private BtraceTracer tracer;
    private BtraceSpanContext context;

    BtraceSpan(
            BtraceTracer tracer,
            String operationName,
            BtraceSpanContext context,
            long startTimestamp,
            Map<String, Object> tags,
            Reference reference) {

        this.tracer = tracer;
        this.context = context;

        this.spanData = SpanData.build(context);
        this.spanData.setName(operationName);
        this.spanData.setTags(tags);
        String kind = (String) tags.get(Tags.SPAN_KIND.getKey());
        if (kind != null) {
            spanData.setKind(kind.toUpperCase());
            Map<String, String> map = new HashMap<String, String>(1);
            if (Constants.Kind.CLIENT.equals(spanData.getKind())) {
                map.put(Constants.LogFields.EVENT, Constants.LogEvent.CLIENT_SEND);
            } else if (Constants.Kind.SERVER.equals(spanData.getKind())) {
                map.put(Constants.LogFields.EVENT, Constants.LogEvent.SERVER_RECEIVE);
            }
            spanData.getLogs().add(SpanData.Log.create(startTimestamp, map));
        }

        this.spanData.setReference(reference == null ? References.CHILD_OF : reference.getType());
        this.spanData.setTimestamp(startTimestamp);
        this.spanData.setEndPoint(SpanData.EndPoint.build(tracer.getServiceName(), tracer.getLocalIpAddress()));
    }


    public SpanData getSpanData() {
        return spanData;
    }

    @Override
    public SpanContext context() {
        return context;
    }

    @Override
    public Span setTag(String key, String value) {
        spanData.getTags().put(key, value);
        if (Tags.SPAN_KIND.getKey().equals(key)) {
            spanData.setKind(value.toUpperCase());
        }
        return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
        spanData.getTags().put(key, value);
        return this;
    }

    @Override
    public Span setTag(String key, Number value) {
        spanData.getTags().put(key, value);
        return this;
    }

    @Override
    public <T> Span setTag(Tag<T> tag, T value) {
        spanData.getTags().put(tag.getKey(), value);
        return this;
    }

    @Override
    public Span log(Map<String, ?> fields) {
        return log(tracer.getClock().currentTimeMicros(), fields);
    }

    @Override
    public Span log(long timestampMicroseconds, Map<String, ?> fields) {
        if (fields == null || fields.size() == 0) {
            return this;
        }
        Map<String, String> saveFields = new HashMap<String, String>();
        Restriction restriction = null;
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            restriction = tracer.getLogFieldRestrictionManager().getRestriction(entry.getKey());
            if ((restriction != null && (!restriction.isKeyAllowed())) || entry.getValue() == null) {
                continue;
            }
            saveFields.put(entry.getKey(),
                    restriction == null ?
                            entry.getValue().toString() :
                            restriction.getRestrictionValue(entry.getValue().toString()));
        }
        if (saveFields.size() > 0) {
            spanData.getLogs().add(SpanData.Log.create(timestampMicroseconds, saveFields));
        }
        return this;
    }

    @Override
    public Span log(String event) {
        return log(tracer.getClock().currentTimeMicros(), event);
    }

    @Override
    public Span log(long timestampMicroseconds, String event) {
        if (event == null || event.length() == 0) {
            return this;
        }
        Restriction eventRestriction = tracer.getLogFieldRestrictionManager().getRestriction(Constants.LogFields.EVENT);
        if (eventRestriction != null && (!eventRestriction.isKeyAllowed())) {
            return this;
        }

        Map<String, String> fields = new HashMap<>();
        fields.put(Constants.LogFields.EVENT,
                eventRestriction == null ? event : eventRestriction.getRestrictionValue(event));

        spanData.getLogs().add(SpanData.Log.create(timestampMicroseconds, fields));
        return this;
    }

    @Override
    public Span setBaggageItem(String key, String value) {
        spanData.getBaggages().put(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        synchronized (this) {
            return spanData.getBaggages().get(key);
        }
    }

    @Override
    public Span setOperationName(String operationName) {
        spanData.setName(operationName);
        return this;
    }

    @Override
    public void finish() {
        long currentTimeMicros = tracer.getClock().currentTimeMicros();
        spanData.setDuration(currentTimeMicros - spanData.getTimestamp());
        helpFinish(currentTimeMicros);
    }

    @Override
    public void finish(long finishMicros) {
        spanData.setDuration(finishMicros - spanData.getTimestamp());
        helpFinish(finishMicros);
    }

    /**
     * 记录时间日志 并 进行report
     * @param finishMicros
     */
    public void helpFinish(long finishMicros) {
        Map<String, String> map = new HashMap<String, String>(1);
        if (Constants.Kind.CLIENT.equals(spanData.getKind())) {
            map.put(Constants.LogFields.EVENT, Constants.LogEvent.CLIENT_RECEIVE);
        } else if (Constants.Kind.SERVER.equals(spanData.getKind())) {
            map.put(Constants.LogFields.EVENT, Constants.LogEvent.SERVER_SEND);
        } else if (Constants.Kind.PRODUCER.equals(spanData.getKind())) {
            map.put(Constants.LogFields.EVENT, Constants.LogEvent.MESSAGE_SEND);
        } else if (Constants.Kind.CONSUMER.equals(spanData.getKind())) {
            map.put(Constants.LogFields.EVENT, Constants.LogEvent.MESSAGE_RECEIVE);
        }
        spanData.getLogs().add(SpanData.Log.create(finishMicros, map));

        if (context.isSampled()) {
            tracer.reportSpan(this);
        }
    }


}
