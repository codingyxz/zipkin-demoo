package com.demoo.plugin.utils.decorator;

import io.opentracing.Span;

/**
 * span 的装饰器
 * Created by freeway on 2017/9/30.
 */
public interface SpanDecorator<T, C> extends ExceptionEvent<T> {

    void onBefore(T target, Object[] args, Span span);

    void onAfter(T target, Object[] args, C result, Span span);

}
