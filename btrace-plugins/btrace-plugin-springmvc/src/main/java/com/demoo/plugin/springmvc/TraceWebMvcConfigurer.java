package com.demoo.plugin.springmvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhxy
 * @Date 2021/6/30 6:45 下午
 */

@Configuration
public class TraceWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    private TracingHandlerInterceptor tracingHandlerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tracingHandlerInterceptor);
    }
}
