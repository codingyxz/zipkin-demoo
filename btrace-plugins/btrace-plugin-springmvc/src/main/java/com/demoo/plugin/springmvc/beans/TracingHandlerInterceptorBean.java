package com.demoo.plugin.springmvc.beans;

import com.demoo.plugin.springmvc.SpanSpringMvcManager;
import com.demoo.plugin.springmvc.TracingHandlerInterceptor;
import io.opentracing.Tracer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author zhxy
 * @Date 2021/6/30 6:21 下午
 */
public class TracingHandlerInterceptorBean implements FactoryBean, InitializingBean {

    private TracingHandlerInterceptor tracingHandlerInterceptor;
    private Tracer tracer;
    private SpanSpringMvcManager spanSpringMvcManager;

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public void setSpanSpringMvcManager(SpanSpringMvcManager spanSpringMvcManager) {
        this.spanSpringMvcManager = spanSpringMvcManager;
    }

    @Override
    public Object getObject() throws Exception {
        return tracingHandlerInterceptor;
    }

    @Override
    public Class<?> getObjectType() {
        return TracingHandlerInterceptor.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assert tracer != null;
        if (spanSpringMvcManager == null) {
            spanSpringMvcManager = new SpanSpringMvcManager();
        }
        tracingHandlerInterceptor = new TracingHandlerInterceptor(tracer, spanSpringMvcManager);
    }
}
