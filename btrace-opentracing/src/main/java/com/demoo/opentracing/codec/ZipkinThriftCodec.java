package com.demoo.opentracing.codec;

import com.demoo.opentracing.BtraceSpan;
import com.demoo.opentracing.Constants;
import com.demoo.opentracing.SpanData;
import com.demoo.opentracing.utils.NetworkUtils;

import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.internal.ThriftCodec;
import zipkin2.internal.V1ThriftSpanWriter;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static zipkin2.internal.HexCodec.lowerHexToUnsignedLong;


/**
 * 与zipkin兼容的 thrift协议的编码类
 *
 */
public class ZipkinThriftCodec implements Codec {

    private static final Logger LOGGER = Logger.getLogger(ZipkinThriftCodec.class.getName());

    public static final String BAGGAGE_PREFIX = "baggage.";
    public static final String LINE_SEPARATOR = "line.separator";

    protected ThriftCodec thriftCodec = new ThriftCodec();
    protected V1ThriftSpanWriter writer = new V1ThriftSpanWriter();
    protected int byteSize = 0;
    protected List<Span> spans = new ArrayList<>();

    private int maxSpanByteSize;

    @Override
    public void maxSpanByteSize(int maxSpanByteSize) {
        this.maxSpanByteSize = maxSpanByteSize;
    }

    @Override
    public int add(BtraceSpan btraceSpan) {
        Span span = convert(btraceSpan);
        int spanSize = thriftCodec.sizeInBytes(span);
        if (spanSize > maxSpanByteSize) {
            LOGGER.warning(String.format("protobuffer received a span that was too large, size = %d, max = %d",
                    spanSize, maxSpanByteSize));
            return byteSize;
        }
        byteSize += spanSize;
        spans.add(span);
        return byteSize;
    }

    @Override
    public int byteSize() {
        return byteSize;
    }

    @Override
    public int spansCount() {
        return spans.size();
    }

    @Override
    public byte[] encode() {
        byte[] bytes = writer.writeList(spans);
        return bytes;
    }

    @Override
    public String getMediaType() {
        return "application/x-thrift";
    }

    @Override
    public void reset() {
        byteSize = 0;
        spans.clear();
    }

    Span convert(BtraceSpan btraceSpan) {
        if (btraceSpan == null) {
            return null;
        }
        SpanData spanData = btraceSpan.getSpanData();
        Span.Builder builder = Span.newBuilder()
                .id(lowerHexToUnsignedLong(spanData.getId()))
                .debug(false)
                .duration(spanData.getDuration())
                .parentId(lowerHexToUnsignedLong(spanData.getParentId()))
                .name(spanData.getName())
                .timestamp(spanData.getTimestamp());

        //build traceId
        String traceId = spanData.getTraceId();
        if (traceId.length() == 32) {
            builder.traceId(String.valueOf(lowerHexToUnsignedLong(traceId, 0)));
        }
//        builder.traceId(String.valueOf(lowerHexToUnsignedLong(traceId)));

        // build endpoint
        Endpoint.Builder endPointBuilder = Endpoint.newBuilder().serviceName(spanData.getEndPoint().getServiceName());
        endPointBuilder.parseIp(NetworkUtils.getInetAddress());
        builder.localEndpoint(endPointBuilder.build());

        for (SpanData.Log log : spanData.getLogs()) {
            String value = "";
            Map<String, String> map = log.getFields();
            if (map.size() == 1) {
                Map.Entry<String, String> entry = map.entrySet().iterator().next();
                if (Constants.LogFields.EVENT.equals(entry.getKey())) {
                    value = entry.getValue();
                } else {
                    value = entry.getKey() + "=" + entry.getValue();
                }
            } else {
                for (Map.Entry<String, String> entry: map.entrySet()) {
                    value += entry.getKey() + "=" + entry.getValue() + "; " + System.getProperty(LINE_SEPARATOR);
                }
            }
            builder.addAnnotation(log.getTimestamp(),value);
        }

        for (Map.Entry<String, Object> entry : spanData.getTags().entrySet()) {
            builder.putTag(entry.getKey(),entry.getValue() == null ? "" : entry.getValue().toString());
        }

        for (Map.Entry<String, String> entry : spanData.getBaggages().entrySet()) {
            builder.putTag(BAGGAGE_PREFIX + entry.getKey(),entry.getValue() == null ? "" : entry.getValue().toString());
        }

        return builder.build();
    }

    @Override
    public Object getRawData() {
        return spans;
    }
}

