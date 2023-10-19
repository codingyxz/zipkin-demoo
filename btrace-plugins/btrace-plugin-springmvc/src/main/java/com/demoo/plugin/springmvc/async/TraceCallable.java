package com.demoo.plugin.springmvc.async;

import com.demoo.plugin.springmvc.SpanSpringMvcManager;
import com.demoo.plugin.springmvc.SpringMvcSpanDecorator;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

import java.util.concurrent.Callable;

/**
 * @author zhxy
 * @Date 2021/6/30 10:09 上午
 */
public class TraceCallable<V> implements Callable<V> {

    /**
     * 提供默认的span name
     */
    private static final String DEFAULT_SPAN_NAME = "async";

    private final Callable<V> delegate;
    private final Tracer.SpanBuilder spanBuilder;
    private final SpanSpringMvcManager spanSpringMvcManager;
    private Tracer tracer;

    public TraceCallable(Tracer tracer, Callable<V> delegate, SpanSpringMvcManager spanSpringMvcManager) {
        this.spanBuilder = tracer.buildSpan(DEFAULT_SPAN_NAME).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        if (tracer.scopeManager().activeSpan() != null) {
            this.spanBuilder.asChildOf(tracer.activeSpan());
        }
        this.tracer = tracer;
        this.delegate = delegate;
        this.spanSpringMvcManager = spanSpringMvcManager;
    }

    @Override
    public V call() throws Exception {

        Span start = spanBuilder.start();
        if (start == null) {
            return this.delegate.call();
        }
        try (Scope scope = tracer.scopeManager().activate(start)) {
            return this.delegate.call();
        } catch (Exception e) {
            for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
                decorator.onException(delegate.getClass().getMethod("call"), new Object[0], e, start);
            }
            throw e;
        } catch (Error e) {
            for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
                decorator.onException(delegate.getClass().getMethod("call"), new Object[0], e, start);
            }
            throw e;
        } finally {
            start.finish();
        }
    }
}
