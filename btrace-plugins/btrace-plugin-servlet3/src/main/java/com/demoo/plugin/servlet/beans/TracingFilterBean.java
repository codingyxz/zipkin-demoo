package com.demoo.plugin.servlet.beans;

import com.demoo.plugin.servlet.SpanServletManager;
import com.demoo.plugin.servlet.TracingFilter;
import io.opentracing.Tracer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author zhxy
 * @Date 2021/6/30 4:18 下午
 */
public class TracingFilterBean implements FactoryBean, InitializingBean {

    private TracingFilter tracingFilter;
    private Tracer tracer;
    private SpanServletManager spanServletManager;

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public void setSpanServletManager(SpanServletManager spanServletManager) {
        this.spanServletManager = spanServletManager;
    }

    @Override
    public Object getObject() throws Exception {
        return tracingFilter;
    }

    @Override
    public Class<?> getObjectType() {
        return TracingFilter.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assert tracer != null;
        if (spanServletManager == null) {
            spanServletManager = new SpanServletManager();
        }
        tracingFilter = new TracingFilter(tracer, spanServletManager);
    }
}
