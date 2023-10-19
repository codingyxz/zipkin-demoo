package com.demoo.btracee1.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhxy
 * @Date 2021/6/29 12:08 上午
 */

@Configuration
public class AppConfig {

    @Bean
    public OkHttpClient okHttpClient(){
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        return okHttpClient;
    }
}
