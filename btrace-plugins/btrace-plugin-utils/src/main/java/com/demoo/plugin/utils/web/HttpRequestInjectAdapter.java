package com.demoo.plugin.utils.web;

import io.opentracing.propagation.TextMap;

import java.util.Iterator;
import java.util.Map;

/**
 * 跨进程注入
 *
 */
public abstract class HttpRequestInjectAdapter<T> implements TextMap {

    protected T request;

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("InjectAdapter should only be used with Tracer.inject()");
    }

    @Override
    public abstract void put(String key, String value);

    public HttpRequestInjectAdapter(T request) {
        this.request = request;
    }
}
