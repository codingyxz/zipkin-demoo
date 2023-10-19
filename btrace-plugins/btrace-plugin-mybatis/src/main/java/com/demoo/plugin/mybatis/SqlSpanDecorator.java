package com.demoo.plugin.mybatis;

import com.demoo.plugin.utils.decorator.SpanDecorator;
import io.opentracing.Span;

/**
 * @author zhxy
 * @Date 2021/7/4 3:51 下午
 */
public class SqlSpanDecorator implements SpanDecorator {


    @Override
    public void onException(Object target, Object[] args, Throwable throwable, Span span) {

    }

    @Override
    public void onBefore(Object target, Object[] args, Span span) {

    }

    @Override
    public void onAfter(Object target, Object[] args, Object result, Span span) {

    }
}
