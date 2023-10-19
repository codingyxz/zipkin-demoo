package com.demoo.btracee2.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhxy
 * @Date 2021/6/29 12:26 上午
 */
@Configuration
public class AppConfig {

    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient().newBuilder().build();
    }
}
