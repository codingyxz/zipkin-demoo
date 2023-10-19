package com.demoo.autoconfig;

import com.demoo.plugin.servlet.SpanServletManager;
import com.demoo.plugin.servlet.TracingFilter;
import io.opentracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static javax.servlet.DispatcherType.*;

/**
 * @author zhxy
 * @Date 2021/6/30 4:39 下午
 */

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(name = "com.demoo.plugin.servlet.TracingFilter")
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(TraceAutoConfiguration.class)
public class TraceServletAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpanServletManager spanServletManager() {
        return new SpanServletManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public TracingFilter tracingFilter(Tracer tracer, SpanServletManager spanServletManager) {
        return new TracingFilter(tracer, spanServletManager);
    }

    @Bean
    public FilterRegistrationBean traceWebFilter(TracingFilter tracingFilter) {
        FilterRegistrationBean<TracingFilter> filterRegistrationBean = new FilterRegistrationBean<>(tracingFilter);

        filterRegistrationBean.setDispatcherTypes(ASYNC, ERROR, FORWARD, INCLUDE, REQUEST);
        filterRegistrationBean.setOrder(TracingFilter.ORDER);
        return filterRegistrationBean;
    }
}
