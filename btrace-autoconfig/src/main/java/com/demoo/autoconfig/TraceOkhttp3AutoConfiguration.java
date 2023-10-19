package com.demoo.autoconfig;

import com.demoo.plugin.okhttp.OkhttpPostProcessorBean;
import com.demoo.plugin.okhttp.SpanOkhttpManager;
import io.opentracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * autoconfig okhttp3
 */
@Configuration
@ConditionalOnBean(Tracer.class)
@ConditionalOnClass({SpanOkhttpManager.class})
@AutoConfigureAfter(TraceAutoConfiguration.class)
public class TraceOkhttp3AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpanOkhttpManager spanOkhttpManager() {
        return new SpanOkhttpManager(true);
    }

    @Bean
    @ConditionalOnMissingBean
    public OkhttpPostProcessorBean okhttpClientPostProcessorBean(Tracer tracer, SpanOkhttpManager spanOkhttpManager) {
        return new OkhttpPostProcessorBean(tracer, spanOkhttpManager);
    }
}
