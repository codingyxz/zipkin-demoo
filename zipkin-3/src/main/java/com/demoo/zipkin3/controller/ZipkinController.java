package com.demoo.zipkin3.controller;


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


    public static final String URL = "http://localhost:8084/zipkin/service4";



    @Autowired
    OkHttpClient client;


    @RequestMapping("/service3")
    public String service() throws IOException {
        System.out.println("---------locading----------");

        Request request = new Request.Builder().url(URL).build();
        Response response = client.newCall(request).execute();


        return "con3 + " + response.body().toString() ;
    }
}
