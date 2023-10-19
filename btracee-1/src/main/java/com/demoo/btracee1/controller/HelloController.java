package com.demoo.btracee1.controller;

import io.opentracing.Tracer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author zhxy
 * @Date 2021/6/29 12:04 上午
 */

@RestController
@RequestMapping("btracee1")
public class HelloController {

    public static final String URL = "http://localhost:8091/btracee2/service";

    @Autowired
    OkHttpClient client;
    @Autowired
    Tracer tracer;


    @GetMapping("/service")
    public String service() {
        System.out.println(tracer.hashCode());
        Request request = new Request.Builder().url(URL).build();
        Response response;
        try {

//            for (int i = 0; i < 10; i++) {
//                response = client.newCall(request).execute();
//                String res = response.body().string();
//            }
            response = client.newCall(request).execute();
            String res = response.body().string();
            System.out.println("btracee1 -- " + res);
            return res;

        } catch (IOException e) {
            e.printStackTrace();

        }
        return "null";
    }
}
