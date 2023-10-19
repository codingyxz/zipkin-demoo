package com.demoo.zipkin2.controller;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("zipkin")
public class ZipkinController {


    public static final String URL1 = "http://localhost:8083/zipkin/service3";
    public static final String URL2 = "http://localhost:8084/zipkin/service4";



    @Autowired
    OkHttpClient client;


    @RequestMapping("/service2")
    public String service() throws IOException {
        System.out.println("---------locading----------");

        Request request1 = new Request.Builder().url(URL1).build();
        Request request2 = new Request.Builder().url(URL2).build();

        Response response1 = client.newCall(request1).execute();
        Response response2 = client.newCall(request2).execute();

        return "con2 + " + response1.body().toString() + "-" + response2.body().toString();
    }
}
