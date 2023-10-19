package com.demoo.plugin.servlet;

import com.demoo.plugin.utils.decorator.HttpRequestSpanDecorator;
import com.demoo.plugin.utils.utils.SpanUtils;
import io.opentracing.Span;
import io.opentracing.tag.Tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SpanDecorator to decorate span at different stages in filter processing
 * before filterChain.doFilter(), after and if exception is thrown.
 *
 * @author zhxy
 * @Date 2021/6/30 1:13 下午
 */
public interface ServletFilterSpanDecorator extends HttpRequestSpanDecorator<HttpServletRequest, HttpServletResponse, Object> {

    static final String HTTP_COMPONENT = "http";


    ServletFilterSpanDecorator DEFAULT_DECORATOR = new ServletFilterSpanDecorator() {

        @Override
        public void onRequest(HttpServletRequest request, Object addition, Span span) {
            SpanUtils.setSpanToLogParameter(span);
            Tags.COMPONENT.set(span, HTTP_COMPONENT);
            Tags.HTTP_METHOD.set(span, request.getMethod());
            // without query params
            Tags.HTTP_URL.set(span, request.getRequestURL().toString());
            SpanUtils.setRequestBaggage(request, span);
        }

        @Override
        public void onResponse(HttpServletResponse response, Span span) throws IOException {
            SpanUtils.setSpanToLogParameter(span);
        }

        @Override
        public void onException(Throwable throwable, Span span) {
            SpanUtils.setSpanToLogParameter(span);
            SpanUtils.logsForException(throwable, span);
        }
    };
}
