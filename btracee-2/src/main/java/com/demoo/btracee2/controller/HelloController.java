package com.demoo.btracee2.controller;

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
@RequestMapping("btracee2")
public class HelloController {

    @GetMapping("/service")
    public String service() {
        System.out.println("btracee2");
       return "haha services";
    }
}
