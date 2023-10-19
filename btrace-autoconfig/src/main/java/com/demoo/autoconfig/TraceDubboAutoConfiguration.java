package com.demoo.autoconfig;

import com.demoo.plugin.dubbo.SpanDubboManager;
import io.opentracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dubbo自动装配
 *
 * @author zhxy
 * @Date 2021/7/4 3:40 下午
 */

@Configuration
@ConditionalOnBean(Tracer.class)
@ConditionalOnClass(name = "com.demoo.plugin.dubbo.SpanDubboManager")
@AutoConfigureAfter(TraceAutoConfiguration.class)
public class TraceDubboAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public SpanDubboManager spanDubboManager() {
        return new SpanDubboManager(true);
    }

}
