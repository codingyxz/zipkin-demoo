package com.demoo.opentracing.codec;

import com.alibaba.fastjson.JSON;
import com.demoo.opentracing.BtraceSpan;
import com.demoo.opentracing.SpanData;
import com.demoo.opentracing.internal.JsonSpan;
import com.demoo.opentracing.utils.NetworkUtils;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class JsonCodec implements Codec {

    private static final Logger LOGGER = Logger.getLogger(JsonCodec.class.getName());

    public static final String LINE_SEPARATOR = "line.separator";
    public static final String BAGGAGE_PREFIX = "baggage.";

    zipkin2.internal.JsonCodec jsonCodec = new zipkin2.internal.JsonCodec();

    private int byteSize;
    private int maxSpanByteSize;
    private List<JsonSpan> jsonSpanList;

    public JsonCodec() {
        this.jsonSpanList = new ArrayList<JsonSpan>();
        reset();
    }

    @Override
    public void maxSpanByteSize(int maxSpanByteSize) {
        this.maxSpanByteSize = maxSpanByteSize;
    }

    @Override
    public int add(BtraceSpan btraceSpan) {
        JsonSpan jsonSpan = convert(btraceSpan.getSpanData());
        String jsonStr = JSON.toJSONString(jsonSpan);
        int spanSize = jsonStr.getBytes(Charset.forName("UTF-8")).length;
        if (spanSize > maxSpanByteSize) {
            LOGGER.warning(String.format("received a span that was too large, size = %d, max = %d",
                    spanSize, maxSpanByteSize));
            return byteSize;
        }
        byteSize += spanSize;
        jsonSpanList.add(jsonSpan);

        return byteSize;
    }

    private JsonSpan convert(SpanData spanData) {
        JsonSpan jsonSpan = new JsonSpan();
        jsonSpan.setServerName(spanData.getEndPoint().getServiceName());
        jsonSpan.setName(spanData.getName());
        jsonSpan.setDuration(spanData.getDuration());
        jsonSpan.setTraceId(spanData.getTraceId());
        jsonSpan.setId(spanData.getId());
        jsonSpan.setParentId(spanData.getParentId());
        jsonSpan.setTimestamp(spanData.getTimestamp());
        jsonSpan.setIp(getIp());

        setTagsAndBaggages(spanData, jsonSpan);
        setLogs(spanData, jsonSpan);

        return jsonSpan;
    }


    private void setTagsAndBaggages(SpanData spanData, JsonSpan jsonSpan) {
        if (spanData.getTags() != null && spanData.getTags().size() > 0) {
            Map<String, Object> tags = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : spanData.getTags().entrySet()) {
                tags.put(entry.getKey(), entry.getValue());
            }
            jsonSpan.setTags(tags);
        }
        if (spanData.getBaggages() != null && spanData.getBaggages().size() > 0) {
            Map<String, String> baggages = new HashMap<String, String>();
            for (Map.Entry<String, String> entry : spanData.getBaggages().entrySet()) {
                baggages.put(entry.getKey(), entry.getValue());
            }
            jsonSpan.setBaggages(baggages);
        }
    }

    private void setLogs(SpanData spanData, JsonSpan jsonSpan) {
        if (spanData.getLogs() == null || spanData.getLogs().size() == 0) {
            return;
        }
        List<JsonSpan.Log> logs = new ArrayList<>();
        for (SpanData.Log log : spanData.getLogs()) {
            if (log.getFields() == null || log.getFields().size() == 0) {
                continue;
            }
            JsonSpan.Log jsonSpanLog = new JsonSpan.Log();
            jsonSpanLog.setTs(log.getTimestamp());
            Map<String, String> fields = new HashMap<>();
            for (Map.Entry<String, String> entry : log.getFields().entrySet()) {
                fields.put(entry.getKey(), entry.getValue());
            }
            jsonSpanLog.setFields(fields);
            logs.add(jsonSpanLog);
        }
        jsonSpan.setLogs(logs);
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
        return jsonSpanList.size();
    }

    @Override
    public byte[] encode() {
        byte[] bytes = JSON.toJSONString(jsonSpanList).getBytes(Charset.forName("UTF-8"));
        System.out.println(new String(bytes));
        return bytes;

    }

    @Override
    public void reset() {
        byteSize = 0;
        jsonSpanList.clear();
    }

    @Override
    public String getMediaType() {
        return "application/json";
    }

    @Override
    public Object getRawData() {
        return jsonSpanList;
    }
}
