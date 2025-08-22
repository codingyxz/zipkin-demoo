package com.demoo.plugin.logback;

import ch.qos.logback.classic.LoggerContext;
import io.opentracing.Tracer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @Description TODO
 * @Date 2025-06-27
 * @Created by Yolo
 */

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogFilterBean implements InitializingBean {

    private Tracer tracer;
    private SpanLogManager spanLogManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        assert tracer != null;
        if (spanLogManager == null) {
            spanLogManager = new SpanLogManager();
        }
        LogCustomFilter logCustomFilter = new LogCustomFilter(tracer, spanLogManager);
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (loggerContext != null && loggerContext.getTurboFilterList() != null) {
            loggerContext.getTurboFilterList().add(logCustomFilter);
        }
    }
}
