package com.demoo.plugin.utils.decorator;

import io.opentracing.Span;

/**
 * 产生异常所触发的事件，可在此事件入口进行异常的装饰
 *
 */
public interface ExceptionEvent<T> {

    void onException(T target, Object[] args, Throwable throwable, Span span);

}
