package com.demoo.autoconfig;

import com.demoo.plugin.springmvc.*;
import io.opentracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * springmvc自动加载
 *
 * @author zhxy
 * @Date 2021/6/30 6:34 下午
 */

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(TracingHandlerInterceptor.class)
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(TraceAutoConfiguration.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class TraceSpringMvcAutoConfiguration {

    @Configuration
    @ConditionalOnClass(WebMvcConfigurer.class)
    @Import(TraceWebMvcConfigurer.class)
    protected static class TraceWebMvcAutoConfiguration {

    }

    @Bean
    @ConditionalOnMissingBean
    public SpanSpringMvcManager spanSpringMvcManager() {
        return new SpanSpringMvcManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceHandlerExceptionResolver traceHandlerExceptionResolver
            (SpanSpringMvcManager spanSpringMvcManager) {
        return new TraceHandlerExceptionResolver(spanSpringMvcManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public TracingHandlerInterceptor tracingHandlerInterceptor
            (Tracer tracer, SpanSpringMvcManager spanSpringMvcManager) {
        return new TracingHandlerInterceptor(tracer, spanSpringMvcManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceWebAspect traceWebAspect(Tracer tracer, SpanSpringMvcManager spanSpringMvcManager) {
        return new TraceWebAspect(tracer, spanSpringMvcManager);
    }
}
