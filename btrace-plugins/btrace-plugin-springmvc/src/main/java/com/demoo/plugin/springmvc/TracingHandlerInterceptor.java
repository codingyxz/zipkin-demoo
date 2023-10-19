package com.demoo.plugin.springmvc;


import com.demoo.plugin.springmvc.utils.SpringMVCUtils;
import com.demoo.plugin.utils.utils.ConstUtils;
import com.demoo.plugin.utils.utils.RequestOperateUtil;
import com.demoo.plugin.utils.web.HttpServletRequestExtractAdapter;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

/**
 * @author zhxy
 * @Date 2021/6/30 11:14 上午
 */
public class TracingHandlerInterceptor implements AsyncHandlerInterceptor {

    private Tracer tracer;
    private SpanSpringMvcManager spanSpringMvcManager;

    public TracingHandlerInterceptor(Tracer tracer) {
        this(tracer, new SpanSpringMvcManager());
    }

    public TracingHandlerInterceptor(Tracer tracer, SpanSpringMvcManager spanSpringMvcManager) {
        this.tracer = tracer;
        this.spanSpringMvcManager = spanSpringMvcManager;
    }

    /**
     * servlet端 告知不进行监控
     *
     * @param httpServletRequest
     * @return
     */
    private boolean isSkipTracing(HttpServletRequest httpServletRequest) {

        Boolean isServletSkipTracing = (Boolean) httpServletRequest.getAttribute(ConstUtils.IS_SKIP_TRACING);
        return tracer == null || tracer instanceof NoopTracer ||
                (isServletSkipTracing != null && isServletSkipTracing) ||
                isSpringMvcSkipTracing(httpServletRequest);
    }

    private boolean isSpringMvcSkipTracing(HttpServletRequest httpServletRequest) {
        String url = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
        for (Pattern pattern : spanSpringMvcManager.getSkipPatterns()) {
            if (pattern.matcher(url).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        RequestContextHolder.currentRequestAttributes();

        // 如果servlet端忽略追踪 或tracer不存在 或空实现 就直接执行结束
        if (isSkipTracing(request)) {
            return true;
        }

        Span span = RequestOperateUtil.getServletSpan(request);
        if (span == null) {
            // 如果为null表明servlet的plugin尚未使用
            // spring 尝试从Header中提取span context
            SpanContext spanContext = tracer.extract(Format.Builtin.HTTP_HEADERS,
                    new HttpServletRequestExtractAdapter(request));
            span = span == null ?
                    tracer.buildSpan(request.getMethod())
                            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                            .start() :
                    tracer.buildSpan(request.getMethod())
                            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                            .asChildOf(spanContext)
                            .start();
        }
        try (Scope scope = tracer.scopeManager().activate(span)){
            for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
                decorator.onPreHandle(request, handler, span);
            }
            SpringMVCUtils.pushSpan(request, span);
        }
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (isSkipTracing(request)) {
            return;
        }
        Span span = SpringMVCUtils.popSpan(request);
        if (span == null) {
            return;
        }

        try (Scope scope = tracer.scopeManager().activate(span)) {
            for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
                decorator.onAfterConcurrentHandlingStarted(request, response, handler, span);
            }
        }
        // 如果没有在filter中缓存，则结束span，否则会在filter中结束span
        if (RequestOperateUtil.getServletSpan(request) == null) {
            span.finish();
        }

        request.getAsyncContext().addListener(new TraceAsyncListener(span, spanSpringMvcManager));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        if (isSkipTracing(request)) {
            return;
        }

        Span span = SpringMVCUtils.popSpan(request);
        if (span == null) {
            return;
        }
        // 这里判断全局异常截获的函数没有触发异常，防止异常重复记录
        SpringMVCUtils.onAfterCompletion(request, response, handler, ex, span, spanSpringMvcManager);

        // 如果没有在filter中缓存，则结束span，否则会在filter中结束span
        if (RequestOperateUtil.getServletSpan(request) == null) {
            span.finish();
        }
    }
}
