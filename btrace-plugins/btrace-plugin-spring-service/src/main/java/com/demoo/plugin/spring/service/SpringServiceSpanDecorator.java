package com.demoo.plugin.spring.service;

import com.demoo.plugin.utils.decorator.ExceptionEvent;
import com.demoo.plugin.utils.utils.SpanUtils;
import io.opentracing.Span;
import io.opentracing.tag.Tags;

import java.lang.reflect.Method;

/**
 * @author zhxy
 * @Date 2021/6/29 7:22 下午
 */
public interface SpringServiceSpanDecorator extends ExceptionEvent<Method> {

    String SERVICE_COMPONENT = "service";
    SpringServiceSpanDecorator DEFAULT_DECORATOR = new SpringServiceSpanDecorator() {

        /**
         * 暂时只支持异常的截获，以降低service的开销
         * @param target
         * @param args
         * @param throwable
         * @param span
         */
        @Override
        public void onException(Method target, Object[] args, Throwable throwable, Span span) {
            Tags.COMPONENT.set(span, SERVICE_COMPONENT);
            SpanUtils.logsForException(throwable, span);
        }
    };
}
