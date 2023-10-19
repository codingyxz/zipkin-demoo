package com.demoo.plugin.okhttp;

import io.opentracing.Tracer;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 添加拦截器
 *
 */
public class OkhttpPostProcessorBean implements BeanPostProcessor {

    private Tracer tracer;
    private SpanOkhttpManager spanOkhttpManager;

    public OkhttpPostProcessorBean(Tracer tracer, SpanOkhttpManager spanOkhttpManager) {
        this.tracer = tracer;
        this.spanOkhttpManager = spanOkhttpManager;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String s) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String s) throws BeansException {
        if (bean instanceof OkHttpClient) {
            OkHttpClient okHttpClient = (OkHttpClient) bean;
            // 添加拦截器
            Interceptor tracingInterceptor = new TracingOkhttpInterceptor(tracer, spanOkhttpManager);
            return okHttpClient.newBuilder().addInterceptor(tracingInterceptor).build();
        }

        return bean;
    }
}
