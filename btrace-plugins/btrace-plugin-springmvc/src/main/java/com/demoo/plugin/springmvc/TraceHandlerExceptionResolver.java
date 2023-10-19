package com.demoo.plugin.springmvc;

import com.demoo.plugin.springmvc.utils.SpringMVCUtils;
import io.opentracing.Span;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhxy
 * @Date 2021/6/30 6:02 下午
 */

@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class TraceHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    private SpanSpringMvcManager spanSpringMvcManager;

    public TraceHandlerExceptionResolver(SpanSpringMvcManager spanSpringMvcManager) {
        this.spanSpringMvcManager = spanSpringMvcManager;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 3;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception e) {

        Span span = SpringMVCUtils.getSpan(httpServletRequest);
        if (span == null) {
            return null;
        }
        SpringMVCUtils.onAfterCompletion(httpServletRequest, httpServletResponse, handler, e, span, spanSpringMvcManager);
        return null;
    }
}
