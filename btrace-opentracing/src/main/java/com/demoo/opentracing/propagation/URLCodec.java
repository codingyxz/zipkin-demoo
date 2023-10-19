package com.demoo.opentracing.propagation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * url 编码
 * Created by freeway on 2017/9/23.
 */
public class URLCodec {


    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // not much we can do, try raw value
            return value;
        }
    }

    public static String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // not much we can do, try raw value
            return value;
        }
    }
}
