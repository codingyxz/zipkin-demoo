package com.demoo.zipkin5.controller;


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



    @Autowired
    OkHttpClient client;


    @RequestMapping("/service5")
    public String service() throws IOException {

        return "service5--------";
    }
}
