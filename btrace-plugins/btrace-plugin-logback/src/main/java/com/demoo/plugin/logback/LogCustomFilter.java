package com.demoo.plugin.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.Marker;

/**
 * 自定义全局logback过滤器
 * @Description TODO
 * @Date 2025-06-27
 * @Created by Yolo
 */
public class LogCustomFilter extends TurboFilter {

    private Tracer tracer;

    private SpanLogManager spanLogManager;

    public LogCustomFilter() {
    }

    public LogCustomFilter(Tracer tracer) {
        this(tracer, new SpanLogManager());
    }

    public LogCustomFilter(Tracer tracer, SpanLogManager spanLogManager) {
        this.tracer = tracer;
        this.spanLogManager = spanLogManager;
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        try {
            if (Level.ERROR.equals(level)) {
                Span parentActiveSpan = tracer.activeSpan();
                if (spanLogManager != null && parentActiveSpan != null) {
                    for (LogFilterSpanDecorator logFilterSpanDecorator : spanLogManager.getDecorators()) {
                        logFilterSpanDecorator.onException(t, parentActiveSpan);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("logbackExcption");
        }
        return FilterReply.NEUTRAL;
    }
}
