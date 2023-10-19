package com.demoo.plugin.utils.decorator;

import io.opentracing.Span;
import java.io.IOException;

/**
 * 针对请求客户端通过resttemplate、httpclient等组件进行http请求的装饰器
 *
 */
public interface HttpRequestSpanDecorator<req, resp, add> {

    /**
     * 请求前调用
     *
     * @param request  请求对象
     * @param addition 附加对象，根据不同的http请求组件的不同有区别
     * @param span     当前activeSpan
     */
    void onRequest(req request, add addition, Span span);

    /**
     * 相应后调用
     *
     * @param response 响应对象
     * @param span
     * @throws IOException
     */
    void onResponse(resp response, Span span) throws IOException;

    /**
     * 请求出现异常调用
     *
     * @param throwable
     * @param span
     */
    void onException(Throwable throwable, Span span);

}
