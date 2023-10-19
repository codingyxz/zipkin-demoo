package com.demoo.zipkin4.controller;


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


    public static final String URL = "http://localhost:8085/zipkin/service5";



    @Autowired
    OkHttpClient client;


    @RequestMapping("/service4")
    public String service() throws IOException {
        System.out.println("---------locading----------");

        Request request = new Request.Builder().url(URL).build();
        Response response = client.newCall(request).execute();


        return "con4 + " + response.body().toString() ;
    }
}
