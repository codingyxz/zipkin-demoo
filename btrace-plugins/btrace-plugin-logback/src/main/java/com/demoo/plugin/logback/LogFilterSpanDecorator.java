package com.demoo.plugin.logback;

import io.opentracing.Span;

/**
 * @Description TODO
 * @Date 2025-06-27
 * @Created by Yolo
 */
public interface LogFilterSpanDecorator {


    void onException(Throwable throwable, Span span);


    LogFilterSpanDecorator DEFAULT_DECORATOR = new LogFilterSpanDecorator() {
        @Override
        public void onException(Throwable throwable, Span span) {
            try {
                if (throwable != null && span != null) {
                    LogUtils.logsForException(throwable, span);
                }
            } catch (Exception e) {
            }
        }
    };

}
