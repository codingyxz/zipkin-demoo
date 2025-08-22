package com.demoo.autoconfig;

import ch.qos.logback.classic.LoggerContext;
import com.demoo.plugin.logback.LogFilterBean;
import com.demoo.plugin.logback.SpanLogManager;
import io.opentracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Description TODO
 * @Date 2025-06-27
 * @Created by Yolo
 */

@Configuration
@ConditionalOnBean(Tracer.class)
@ConditionalOnClass({LoggerContext.class, SpanLogManager.class})
@AutoConfigureAfter(TraceAutoConfiguration.class)
public class TraceLogbackAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpanLogManager spanLogManager() {
        return new SpanLogManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogFilterBean logFilterBean(Tracer tracer, SpanLogManager spanLogManager) {
        return new LogFilterBean(tracer, spanLogManager);
    }

}
