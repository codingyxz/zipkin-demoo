package com.demoo.plugin.springmvc;

import io.opentracing.Span;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 异步请求监听器
 *
 * @author zhxy
 * @Date 2021/6/30 5:47 下午
 */
public class TraceAsyncListener implements AsyncListener {

    private Span span;
    private SpanSpringMvcManager spanSpringMvcManager;

    public TraceAsyncListener(Span span, SpanSpringMvcManager spanSpringMvcManager) {
        this.span = span;
        this.spanSpringMvcManager = spanSpringMvcManager;
    }

    @Override
    public void onComplete(AsyncEvent asyncEvent) throws IOException {

    }

    @Override
    public void onTimeout(AsyncEvent asyncEvent) throws IOException {
        for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
            decorator.onAsyncTimeout(
                    (HttpServletRequest) asyncEvent.getSuppliedRequest(),
                    (HttpServletResponse) asyncEvent.getSuppliedResponse(),
                    asyncEvent.getAsyncContext().getTimeout(),
                    span
            );
        }
    }

    @Override
    public void onError(AsyncEvent asyncEvent) throws IOException {
        for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
            decorator.onAsyncError(
                    (HttpServletRequest) asyncEvent.getSuppliedRequest(),
                    (HttpServletResponse) asyncEvent.getSuppliedResponse(),
                    asyncEvent.getThrowable(),
                    span
            );
        }
    }

    @Override
    public void onStartAsync(AsyncEvent asyncEvent) throws IOException {

    }
}
