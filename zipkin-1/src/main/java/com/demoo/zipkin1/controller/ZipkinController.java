package com.demoo.zipkin1.controller;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("zipkin")
public class ZipkinController {


    public static final String URL = "http://localhost:8082/zipkin/service2";


    @Autowired
    OkHttpClient client;


    @GetMapping("/service1")
    public String service() {
        Request request = new Request.Builder().url(URL).build();
        Response response;
        try {
            response = client.newCall(request).execute();
            return response.body().string();

        } catch (IOException e) {
            e.printStackTrace();

        }
        return "null";
    }

}
