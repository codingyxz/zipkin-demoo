package com.demoo.plugin.okhttp;

import com.demoo.plugin.utils.web.HttpRequestInjectAdapter;
import okhttp3.Request;

/**
 * 注入tracing信息到http header
 *
 */
public class OkhttpRequestInjectAdapter extends HttpRequestInjectAdapter<Request.Builder> {

    public OkhttpRequestInjectAdapter(Request.Builder requestBuilder) {
        super(requestBuilder);
    }

    @Override
    public void put(String key, String value) {
        this.request.addHeader(key, value);
    }
}
