package com.demoo.zipkin3.config;


import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.okhttp.BraveOkHttpRequestResponseInterceptor;
import com.github.kristofa.brave.servlet.BraveServletFilter;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpanCollectorConfig {


    @Value("${zipkin.url}")
    private String url;


    @Value("${zipkin.serviceName}")
    private String serviceName;

    /**
     * 连接超时时间
     */
    @Value("${zipkin.connectTimeout}")
    private int connectTimeout;


    /**
     * 是否启动压缩
     */
    @Value("${zipkin.compressionEnabled}")
    private boolean compressionEnable;


    /**
     * 上传span的时间间隔
     */
    @Value("${zipkin.flushInterval}")
    private int flushInterval;


    /**
     * 读取超时时间
     */
    @Value("${zipkin.readTimeout}")
    private int readTimeout;


    @Value("${zipkin.samplerRate}")
    private float samplerRate;


    /**
     * 配置span收集器
     *
     * @return
     */
    @Bean
    public SpanCollector spanCollector() {

        HttpSpanCollector.Config config = HttpSpanCollector.Config.builder()
                .connectTimeout(connectTimeout)
                .compressionEnabled(compressionEnable)
                .flushInterval(flushInterval)
                .readTimeout(readTimeout)
                .build();

        return HttpSpanCollector.create(url, config, new EmptySpanCollectorMetricsHandler());

    }


    /**
     * 配置采集率
     *
     * @param spanCollector
     * @return
     */
    @Bean
    public Brave brave(SpanCollector spanCollector) {
        Brave.Builder builder = new Brave.Builder(serviceName);
        builder.spanCollector(spanCollector)
                .traceSampler(Sampler.create(samplerRate))
                .build();
        return builder.build();
    }


    /**
     * 设置server的（服务端收到请求和服务端完成处理，并将结果发送到客户端）过滤器
     *
     * @param brave
     * @return
     */
    @Bean
    public BraveServletFilter braveServletFilter(Brave brave) {
        BraveServletFilter filter = new BraveServletFilter(brave.serverRequestInterceptor(),
                brave.serverResponseInterceptor(), new DefaultSpanNameProvider());

        return filter;
    }


    /**
     * 设置client的rs和cs的拦截器
     * @param brave
     * @return  OkHttpClient返回请求实例
     */
    @Bean
    public OkHttpClient okHttpClient(Brave brave) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new BraveOkHttpRequestResponseInterceptor(
                        brave.clientRequestInterceptor(),
                        brave.clientResponseInterceptor(),
                        new DefaultSpanNameProvider())).build();
        return httpClient;

    }
}
