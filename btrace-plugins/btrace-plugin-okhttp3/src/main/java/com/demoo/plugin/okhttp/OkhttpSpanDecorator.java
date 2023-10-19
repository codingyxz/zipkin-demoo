package com.demoo.plugin.okhttp;

import com.demoo.plugin.utils.decorator.HttpRequestSpanDecorator;
import com.demoo.plugin.utils.utils.SpanUtils;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

/**
 * okhttp 默认装饰器
 *
 */
public interface OkhttpSpanDecorator extends HttpRequestSpanDecorator<Request, Response, Object> {

    String HTTP_COMPONENT = "http";

    OkhttpSpanDecorator DEFAULT_DECORATOR = new OkhttpSpanDecorator() {

        /**
         * 请求前调用
         *
         * @param request  请求对象
         * @param addition 附加对象，根据不同的http请求组件的不同有区别
         * @param span     当前activeSpan
         */
        @Override
        public void onRequest(Request request, Object addition, Span span) {
            Tags.COMPONENT.set(span, HTTP_COMPONENT);
            Tags.HTTP_METHOD.set(span, request.method());
            Tags.HTTP_URL.set(span, request.url().url().toExternalForm());
        }

        /**
         * 响应后调用
         *
         * @param response 响应对象
         * @param span
         * @throws IOException
         */
        @Override
        public void onResponse(Response response, Span span) throws IOException {
            int code = response.code();
            Tags.HTTP_STATUS.set(span, code);
            SpanUtils.logsForMessage(String.valueOf(code), span, !response.isSuccessful());
        }

        /**
         * 请求出现异常调用
         *
         * @param throwable
         * @param span
         */
        @Override
        public void onException(Throwable throwable, Span span) {
            if (throwable != null && span != null) {
                SpanUtils.logsForException(throwable, span);
            }
        }
    };

}
