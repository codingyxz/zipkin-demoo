package com.demoo.opentracing.propagation;

import com.demoo.opentracing.BtraceSpanContext;
import com.demoo.opentracing.Constants;
import com.demoo.opentracing.restrictions.Restriction;
import io.opentracing.propagation.TextMap;

import java.util.HashMap;
import java.util.Map;

/**
 * This format is compatible with other trace libraries such as Brave, Wingtips, zipkin-js, etc.
 * See <a href="http://zipkin.io/pages/instrumenting.html">Instrumenting a Library</a>
 */
public final class B3TextMapCodec extends TextMapCodec {
    private static final PrefixedKeys keys = new PrefixedKeys();

    static final String TRACE_ID_NAME = "X-B3-TraceId";
    static final String SPAN_ID_NAME = "X-B3-SpanId";
    static final String PARENT_SPAN_ID_NAME = "X-B3-ParentSpanId";
    static final String SAMPLED_NAME = "X-B3-Sampled";
    static final String FLAGS_NAME = "X-B3-Flags";
    // NOTE: uber's flags aren't the same as B3/Finagle ones
    static final byte SAMPLED_FLAG = 1;
    static final byte DEBUG_FLAG = 2;

    private final String baggagePrefix;

    private final boolean urlEncoding;

    public B3TextMapCodec(boolean urlEncoding) {
        this(newBuilder().withUrlEncoding(urlEncoding));
    }

    private B3TextMapCodec(Builder builder) {
        this.baggagePrefix = builder.baggagePrefix;
        this.urlEncoding = builder.urlEncoding;
    }

    @Override
    public void inject(BtraceSpanContext spanContext, TextMap carrier) {
        carrier.put(TRACE_ID_NAME, spanContext.getTraceId());
        carrier.put(PARENT_SPAN_ID_NAME, spanContext.getParentId());

        carrier.put(SPAN_ID_NAME, spanContext.getSpanId());
        carrier.put(SAMPLED_NAME, spanContext.isSampled() ? "1" : "0");
        if (spanContext.isDebug()) {
            carrier.put(FLAGS_NAME, "1");
        }
        for (Map.Entry<String, String> entry : spanContext.baggageItems()) {
            Restriction restriction = baggageRestrictionManager.getRestriction(entry.getKey());
            if (restriction == null) {
                carrier.put(keys.prefixedKey(entry.getKey(), baggagePrefix),
                        urlEncoding? URLCodec.encode(entry.getValue()):entry.getValue());
            }  else if (restriction.isKeyAllowed()) {
                carrier.put(keys.prefixedKey(entry.getKey(), baggagePrefix),
                        restriction.getRestrictionValue(
                        urlEncoding? URLCodec.encode(entry.getValue()):entry.getValue()));
            }
        }
    }

    @Override
    public BtraceSpanContext extract(TextMap carrier) {
        String traceId = null;
        String spanId = null;
        String parentId = null;
        Map<String, String> baggages = null;
        byte flags = 0;
        for (Map.Entry<String, String> entry : carrier) {
            if (entry.getKey().equalsIgnoreCase(SAMPLED_NAME)) {
                if (entry.getValue().equals("1") || entry.getValue().toLowerCase().equals("true")) {
                    flags |= SAMPLED_FLAG;
                }
            } else if (entry.getKey().equalsIgnoreCase(TRACE_ID_NAME)) {
                traceId = entry.getValue();
            } else if (entry.getKey().equalsIgnoreCase(PARENT_SPAN_ID_NAME)) {
                parentId = entry.getValue();
            } else if (entry.getKey().equalsIgnoreCase(SPAN_ID_NAME)) {
                spanId = entry.getValue();
            } else if (entry.getKey().equalsIgnoreCase(FLAGS_NAME)) {
                if (entry.getValue().equals("1")) {
                    flags |= DEBUG_FLAG;
                }
            } if (entry.getKey().startsWith(baggagePrefix)) {
                if (baggages == null) {
                    baggages = new HashMap<String, String>(0);
                }
                String realKey = keys.unPrefixedKey(entry.getKey(), baggagePrefix);
                Restriction restriction = baggageRestrictionManager.getRestriction(realKey);
                if (restriction == null) {
                    baggages.put(realKey, urlEncoding ?
                                    URLCodec.decode(entry.getValue()) :
                                    entry.getValue());
                } else if (restriction.isKeyAllowed()) {
                    baggages.put(realKey,
                            restriction.getRestrictionValue(urlEncoding ?
                                    URLCodec.decode(entry.getValue()) :
                                    entry.getValue())
                    );
                }
            }
        }

        if (traceId != null && spanId != null) {
            return new BtraceSpanContext(traceId, spanId, parentId, flags, baggages);
        }
        return null;
    }


    /**
     * Returns a newBuilder for TextMapCodec.
     *
     * @return Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private boolean urlEncoding;
        private String baggagePrefix = Constants.BAGGAGE_KEY_PREFIX;

        public Builder withUrlEncoding(boolean urlEncoding) {
            this.urlEncoding = urlEncoding;
            return this;
        }

        public Builder withBaggagePrefix(String baggagePrefix) {
            this.baggagePrefix = baggagePrefix;
            return this;
        }

        public B3TextMapCodec build() {
            return new B3TextMapCodec(this);
        }
    }
}
