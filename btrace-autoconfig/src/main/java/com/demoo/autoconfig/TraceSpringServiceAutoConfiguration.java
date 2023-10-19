package com.demoo.autoconfig;

import com.demoo.plugin.spring.service.SpanSpringServiceManager;
import com.demoo.plugin.spring.service.TraceServiceAspect;
import io.opentracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author zhxy
 * @Date 2021/6/29 7:59 下午
 */

@Configuration
@ConditionalOnBean(Tracer.class)
@ConditionalOnClass(SpanSpringServiceManager.class)
@AutoConfigureAfter(TraceAutoConfiguration.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class TraceSpringServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpanSpringServiceManager spanSpringServiceManager() {
        return new SpanSpringServiceManager(true);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceServiceAspect traceServiceAspect(Tracer tracer, SpanSpringServiceManager spanSpringServiceManager) {
        return new TraceServiceAspect(tracer, spanSpringServiceManager);
    }

}
