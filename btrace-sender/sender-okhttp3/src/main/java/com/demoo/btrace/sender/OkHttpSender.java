package com.demoo.btrace.sender;

import com.alibaba.fastjson.JSON;
import com.demoo.opentracing.BtraceSpan;
import com.demoo.opentracing.codec.Codec;
import com.demoo.opentracing.senders.AbstractBuilder;
import com.demoo.opentracing.senders.Sender;
import io.opentracing.Span;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author zhxy
 * @Date 2021/6/28 12:57 下午
 */
public class OkHttpSender implements Sender {

    private static final Logger LOGGER = Logger.getLogger(OkHttpSender.class.getName());

    private OkHttpClient httpClient;
    private Request.Builder requestBuilder;
    private int maxSpansBytes;
    private Codec codec;
    private String appKey;
    private String appSecret;
    private boolean isNeedSign = false;

    private OkHttpSender(String endpoint, int maxSpansBytes, Codec codec, OkHttpClient client, String appKey, String appSecret) {
        HttpUrl collectorUrl = HttpUrl
                .parse(String.format("%s", endpoint));
        if (collectorUrl == null) {
            throw new IllegalArgumentException("Could not parse url.");
        }
        this.httpClient = client;
        this.requestBuilder = new Request.Builder().url(collectorUrl);
        this.maxSpansBytes = maxSpansBytes;
        this.codec = codec;
        this.appKey = appKey;
        this.appSecret = appSecret;
        if (!"".equals(appKey) && !"".equals(appSecret)) {
            isNeedSign = true;
        }
    }


    public static Builder newBuilder(String endpoint) {
        return new Builder(endpoint);
    }

    public static class Builder extends AbstractBuilder {

        private String appKey;
        private String appSecret;

        Builder(String endpoint) {
            super(endpoint);
        }

        public Builder withAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder withAppSecret(String appSecret) {
            this.appSecret = appSecret;
            return this;
        }

        public Sender build() {
            OkHttpClient.Builder build = new OkHttpClient.Builder();
            build.readTimeout(2, TimeUnit.SECONDS)
                    .connectTimeout(5, TimeUnit.SECONDS);
            return new OkHttpSender(endpoint, maxSpansByteSize, codec, build.build(), appKey, appSecret);
        }
    }

    @Override
    public int append(Span span) {
        long byteSize = this.codec.add(((BtraceSpan) span));

//        if (byteSize <= maxSpansBytes) {
//            return 0;
//        }
        return flush();
    }

    @Override
    public int flush() {
        if (this.codec.byteSize() == 0) {
            return 0;
        }
        Response response = null;
        try {
            RequestBody body = null;
            if (isNeedSign) {
                requestBuilder.headers(collectHeaderParam("btrace_topic", JSON.toJSONString(codec.getRawData())));
                body = new FormBody.Builder().add("req_data", JSON.toJSONString(codec.getRawData())).build();
            } else {
                body = RequestBody.create(MediaType.parse(codec.getMediaType()), codec.encode());
            }
            Request request = requestBuilder.post(body).build();
            response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                String responseBody;
                try {
                    responseBody = response.body() != null ? response.body().string() : "null";
                } catch (IOException e) {
                    responseBody = "unable to read response";
                }
                LOGGER.warning(String.format("Could not send %d spans, response %d: %s",
                        this.codec.spansCount(), response.code(), responseBody));
                return 0;
            }
            return this.codec.byteSize();
        } catch (Exception e) {
            LOGGER.warning(String.format("Could not send %d spans", this.codec.spansCount()));
            return 0;
        } finally {
            if (response != null) {
                response.close();
            }
            this.codec.reset();
        }
    }

    @Override
    public int close() {
        return flush();
    }

    @Override
    public void send(String topic, String json) {
        Response response = null;
        try {
            RequestBody body = null;
            if (isNeedSign) {
                requestBuilder.headers(collectHeaderParam(topic, json));
                body = new FormBody.Builder().add("req_data", json).build();
            } else {
                body = RequestBody.create(MediaType.parse(codec.getMediaType()), codec.encode());
            }
            Request request = requestBuilder.post(body).build();
            response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                String responseBody;
                try {
                    responseBody = response.body() != null ? response.body().string() : "null";
                } catch (IOException e) {
                    responseBody = "unable to read response";
                }
                LOGGER.warning(String.format("Could not send %d json, response %d: %s",
                        this.codec.spansCount(), response.code(), responseBody));
            }
        } catch (Exception e) {
            LOGGER.warning(String.format("Could not send json, %s", json));
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private Headers collectHeaderParam(String topic, String data) {
        try {
            Headers.Builder headersBuilder = new Headers.Builder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
            String timestamp = sdf.format(new Date());
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("topic", topic);
            map.put("req_data", data);
            map.put("timestamp", timestamp);
            map.put("method", "guahao.gtrace.berichten");
            map.put("appkey", appKey);
            String sign = SignUtil.getInstance().getSign(map, appSecret);
            headersBuilder.add("sign", sign);
            headersBuilder.add("method", "guahao.gtrace.berichten");
            headersBuilder.add("timestamp", timestamp);
            headersBuilder.add("appkey", appKey);
            return headersBuilder.build();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warning("Signature failure, please check.");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warning("Signature failure, please check.");
        }
        return null;
    }

}
