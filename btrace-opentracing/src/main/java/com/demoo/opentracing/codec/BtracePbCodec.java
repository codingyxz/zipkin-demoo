package com.demoo.opentracing.codec;



import com.demoo.codec.PbSpan;
import com.demoo.opentracing.BtraceSpan;
import com.demoo.opentracing.SpanData;
import com.demoo.opentracing.utils.NetworkUtils;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by zhanggn on 2017/11/14.
 */
public class BtracePbCodec implements Codec {

    private static final Logger LOGGER = Logger.getLogger(BtracePbCodec.class.getName());

    private PbSpan.SpanList.Builder spanListBuilder;
    private int byteSize;
    private int maxSpanByteSize;

    public BtracePbCodec() {
        spanListBuilder = PbSpan.SpanList.newBuilder();
        reset();
    }

    @Override
    public void maxSpanByteSize(int maxSpanByteSize) {
        this.maxSpanByteSize = maxSpanByteSize;
    }

    @Override
    public int add(BtraceSpan btraceSpan) {
        PbSpan.Span span = convert(btraceSpan.getSpanData());
        int spanSize = span.getSerializedSize();
        if (spanSize > maxSpanByteSize) {
            LOGGER.warning(String.format("protobuffer received a span that was too large, size = %d, max = %d",
                    spanSize, maxSpanByteSize));
            return byteSize;
        }
        byteSize += spanSize;
        spanListBuilder.addSpans(span);
        return byteSize;
    }

    private PbSpan.Span convert(SpanData spanData) {
        PbSpan.Span.Builder spanBuilder = PbSpan.Span.newBuilder();
        spanBuilder.setId(spanData.getId()).setDuration(spanData.getDuration()).setName(spanData.getName())
                .setParentId(spanData.getParentId()).setServiceName(spanData.getEndPoint() == null ? null : spanData.getEndPoint().getServiceName())
                .setTimestamp(spanData.getTimestamp()).setTraceId(spanData.getTraceId()).setIp(getIp())
                .setKind(getKind(spanData.getKind()))
                .setReference(getReference(spanData.getReference()));
        this.setLogs(spanData, spanBuilder);
        this.setTagsAndBaggages(spanData, spanBuilder);
        return spanBuilder.build();
    }

    private PbSpan.Span.Kind getKind(String spanDataKind) {
        if (spanDataKind == null) {
            return PbSpan.Span.Kind.KIND_UNKNOWN;
        }
        for (PbSpan.Span.Kind kind : PbSpan.Span.Kind.values()) {
            if (kind.name().equals(spanDataKind.toUpperCase())) {
                return kind;
            }
        }
        return PbSpan.Span.Kind.KIND_UNKNOWN;
    }

    private PbSpan.Span.Reference getReference(String spanDataReference) {
        if (spanDataReference == null) {
            return PbSpan.Span.Reference.REFERENCE_UNKNOWN;
        }
        for (PbSpan.Span.Reference reference : PbSpan.Span.Reference.values()) {
            if (reference.name().equals(spanDataReference.toUpperCase())) {
                return reference;
            }
        }
        return PbSpan.Span.Reference.REFERENCE_UNKNOWN;
    }

    private void setLogs(SpanData spanData, PbSpan.Span.Builder spanBuilder) {
        if (spanData.getLogs() == null || spanData.getLogs().size() == 0) {
            return;
        }
        for (SpanData.Log log : spanData.getLogs()) {
            if (log.getFields() == null || log.getFields().size() == 0) {
                continue;
            }
            for (Map.Entry<String, String> entry : log.getFields().entrySet()) {
                PbSpan.Span.Log.Builder logBuilder = PbSpan.Span.Log.newBuilder();
                logBuilder.setKey(entry.getKey());
                logBuilder.setValue(entry.getValue());
                logBuilder.setTimestamp(log.getTimestamp());
                spanBuilder.addLogs(logBuilder.build());
            }
        }
    }

    private void setTagsAndBaggages(SpanData spanData, PbSpan.Span.Builder spanBuilder) {
        if (spanData.getTags() != null && spanData.getTags().size() > 0) {
            for (Map.Entry<String, Object> entry : spanData.getTags().entrySet()) {
                PbSpan.Span.Pair.Builder pairBuilder = PbSpan.Span.Pair.newBuilder();
                pairBuilder.setKey(entry.getKey());
                pairBuilder.setValue(entry.getValue().toString());
                spanBuilder.addTags(pairBuilder.build());
            }
        }
        if (spanData.getBaggages() != null && spanData.getBaggages().size() > 0) {
            for (Map.Entry<String, String> entry : spanData.getBaggages().entrySet()) {
                PbSpan.Span.Pair.Builder pairBuilder = PbSpan.Span.Pair.newBuilder();
                pairBuilder.setKey(entry.getKey());
                pairBuilder.setValue(entry.getValue());
                spanBuilder.addBaggages(pairBuilder.build());
            }
        }
    }


    private int getIp() {
        InetAddress addr = NetworkUtils.getInetAddress();
        if (addr != null) {
            byte[] bytes = addr.getAddress();
            if (bytes.length == 4) {
                return ByteBuffer.wrap(bytes).getInt();
            }
        }
        return 0;
    }


    @Override
    public int byteSize() {
        return byteSize;
    }

    @Override
    public int spansCount() {
        return spanListBuilder.build().getSpansCount();
    }

    @Override
    public byte[] encode() {
        return spanListBuilder.build().toByteArray();
    }

    @Override
    public void reset() {
        byteSize = 0;
        spanListBuilder.clear();
    }

    @Override
    public String getMediaType() {
        return "application/x-protobuf";
    }

    @Override
    public Object getRawData() {
        return null;
    }
}

