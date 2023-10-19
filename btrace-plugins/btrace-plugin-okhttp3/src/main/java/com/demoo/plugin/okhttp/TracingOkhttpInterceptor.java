/*
 * Copyright (c) 2001-2019 GuaHao.com Corporation Limited. All rights reserved.
 * This software is the confidential and proprietary information of GuaHao Company.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with GuaHao.com.
 */
package com.demoo.plugin.okhttp;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * OkHttp拦截器
 *
 */
public class TracingOkhttpInterceptor implements Interceptor {

    private Tracer tracer;

    private SpanOkhttpManager spanOkhttpManager;

    public TracingOkhttpInterceptor(Tracer tracer, SpanOkhttpManager spanOkhttpManager) {
        this.tracer = tracer;
        this.spanOkhttpManager = spanOkhttpManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();
        Span parentSpan = tracer.scopeManager().activeSpan();
        SpanContext spanContext = parentSpan == null ? null : parentSpan.context();
        String spanName = getSpanName(request);
        // 获取当前span
        Span span = spanContext == null ?
            tracer.buildSpan(spanName).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start() :
            tracer.buildSpan(spanName).asChildOf(spanContext).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .start();
        try (Scope scope = tracer.scopeManager().activate(span)) {
            final Request.Builder requestBuilder = request.newBuilder();
            //注入trace信息到header
            tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new OkhttpRequestInjectAdapter(requestBuilder));
            Request newRequest = requestBuilder.build();
            //执行装饰器操作
            for (OkhttpSpanDecorator okhttpSpanDecorator : spanOkhttpManager.getDecorators()) {
                okhttpSpanDecorator.onRequest(request, null, span);
            }
            Response response = chain.proceed(newRequest);
            //执行装饰器操作
            for (OkhttpSpanDecorator okhttpSpanDecorator : spanOkhttpManager.getDecorators()) {
                okhttpSpanDecorator.onResponse(response, span);
            }
            return response;
        } catch (IOException ioe) {
            handleException(ioe, span);
            throw ioe;
        } catch (Exception e) {
            handleException(e, span);
            throw new RuntimeException(e);
        } finally {
            span.finish();
        }
    }

    /**
     * 处理异常
     * @param e
     * @param span
     */
    private void handleException(Exception e, Span span) {
        for (OkhttpSpanDecorator okhttpSpanDecorator : spanOkhttpManager.getDecorators()) {
            okhttpSpanDecorator.onException(e, span);
        }
    }

    /**
     * 获取span名称
     * @param request
     * @return
     */
    private String getSpanName(Request request) {
        return request.url().host() + request.url().encodedPath();
    }
}
