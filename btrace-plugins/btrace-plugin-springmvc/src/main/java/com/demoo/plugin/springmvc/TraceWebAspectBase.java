package com.demoo.plugin.springmvc;

import com.demoo.plugin.springmvc.async.TraceCallable;
import com.demoo.plugin.springmvc.utils.SpringMVCUtils;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author zhxy
 * @Date 2021/6/30 9:55 上午
 */
public class TraceWebAspectBase {


    protected SpanSpringMvcManager spanSpringMvcManager;
    protected Tracer tracer;

    public TraceWebAspectBase(Tracer tracer) {
        this(tracer,new SpanSpringMvcManager());
    }

    public TraceWebAspectBase(Tracer tracer, SpanSpringMvcManager spanSpringMvcManager) {
        this.spanSpringMvcManager = spanSpringMvcManager;
        this.tracer = tracer;
    }

    protected Object invoke(ProceedingJoinPoint pjp) throws Throwable {
        if (tracer instanceof NoopTracer) {
            return pjp.proceed();
        }
        Span span = tracer.scopeManager().activeSpan();
        if (span == null) {
            return pjp.proceed();
        }

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
            decorator.onBefore(method, pjp.getArgs(), span);
        }

        try {
            Object returnObject = pjp.proceed();
            for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
                decorator.onAfter(method, pjp.getArgs(), returnObject, span);
            }
            return returnObject;
        } catch (Throwable throwable) {
            HttpServletRequest request = getRequest();
            if (request != null) {
                SpringMVCUtils.setThrowable(request, throwable);
            }
            for (SpringMvcSpanDecorator decorator : spanSpringMvcManager.getDecorators()) {
                decorator.onException(method, pjp.getArgs(), throwable, span);
            }
            throw throwable;
        }
    }


    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public Object invokeReturningCallable(ProceedingJoinPoint pjp) throws Throwable {
        if (tracer instanceof NoopTracer || tracer.scopeManager().activeSpan() == null) {
            return pjp.proceed();
        }
        final Callable<Object> callable = (Callable<Object>) pjp.proceed();
        if (this.tracer.scopeManager().activeSpan() != null) {
            return new TraceCallable<Object>(this.tracer, callable, spanSpringMvcManager);
        } else {
            return callable;
        }

    }

    public Object invokeReturningWebAsyncTask(ProceedingJoinPoint pjp) throws Throwable {
        if (tracer instanceof NoopTracer || tracer.scopeManager().activeSpan() == null) {
            return pjp.proceed();
        }

        final WebAsyncTask<?> webAsyncTask = (WebAsyncTask<?>) pjp.proceed();
        if (this.tracer.scopeManager().activeSpan() != null) {
            try {
                Field callable = WebAsyncTask.class.getDeclaredField("callable");
                callable.setAccessible(true);
                callable.set(webAsyncTask, new TraceCallable<>(tracer, webAsyncTask.getCallable(), spanSpringMvcManager));
            } catch (NoSuchFieldException ignored) {

            }
        }
        return webAsyncTask;
    }
}
