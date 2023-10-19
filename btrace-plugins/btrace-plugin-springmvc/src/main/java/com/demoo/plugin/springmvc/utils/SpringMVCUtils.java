package com.demoo.plugin.springmvc.utils;

import com.demoo.plugin.springmvc.SpanSpringMvcManager;
import com.demoo.plugin.springmvc.SpringMvcSpanDecorator;
import io.opentracing.Span;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author zhxy
 * @Date 2021/6/29 9:00 下午
 */
public class SpringMVCUtils {

    private static final String SPAN_STACK = "";
    public static final String THROW_EXCEPTION = SpringMVCUtils.class.getName() + ".exception";

    public static void onAfterCompletion(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex,
                                         Span span,
                                         SpanSpringMvcManager spanSpringMvcManager) {
        Throwable throwable = SpringMVCUtils.getThrowable(request);
        if (throwable == null) {
            SpringMVCUtils.setThrowable(request, ex);
            for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
                decorator.onAfterCompletion(request, response, handler, ex, span);
            }
        } else if (ex != null && throwable != ex) {
            // 该异常和先前的异常不一致，需再处理
            SpringMVCUtils.setThrowable(request, ex);
            for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
                decorator.onAfterCompletion(request, response, handler, ex, span);
            }
        } else {
            // 该异常和先前的异常一致，不需要处理异常
            for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
                decorator.onAfterCompletion(request, response, handler, ex, span);
            }
        }
    }


    //----------------------------------------------------------------------------------------------------------------

    /**
     * 设置异常
     *
     * @param request
     * @param throwable
     */

    public static void setThrowable(HttpServletRequest request, Throwable throwable) {
        request.setAttribute(THROW_EXCEPTION, throwable);
    }

    public static Throwable getThrowable(HttpServletRequest request) {
        return (Throwable) request.getAttribute(THROW_EXCEPTION);
    }

    public static Span getSpan(HttpServletRequest request) {
        Deque<Span> stack = getSpanStack(request);
        return stack.peek();
    }

    public static Span popSpan(HttpServletRequest request) {
        Deque<Span> stack = getSpanStack(request);
        return stack.pop();
    }

    public static void pushSpan(HttpServletRequest request, Span span) {
        Deque<Span> stack = getSpanStack(request);
        stack.push(span);
    }

    private static Deque<Span> getSpanStack(HttpServletRequest request) {
        Deque<Span> stack = (Deque<Span>) request.getAttribute(SPAN_STACK);
        if (stack == null) {
            stack = new ArrayDeque<>();
            request.setAttribute(SPAN_STACK, stack);
        }
        return stack;
    }


}
