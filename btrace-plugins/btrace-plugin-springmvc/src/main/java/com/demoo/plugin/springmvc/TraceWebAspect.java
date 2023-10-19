package com.demoo.plugin.springmvc;

import io.opentracing.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author zhxy
 * @Date 2021/6/30 9:55 上午
 */

@Aspect
public class TraceWebAspect extends TraceWebAspectBase {

    public TraceWebAspect(Tracer tracer) {
        super(tracer);
    }

    public TraceWebAspect(Tracer tracer, SpanSpringMvcManager spanSpringMvcManager) {
        super(tracer, spanSpringMvcManager);
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    private void anyRestControllerAnnotated() {
    }

    @Pointcut("@within(org.springframework.stereotype.Controller)")
    private void anyControllerAnnotated() {
    }

    @Pointcut("(anyRestControllerAnnotated() || anyControllerAnnotated())")
    private void anyControllerOrRestController() {
    }

    @Pointcut("execution(public java.util.concurrent.Callable *(..))")
    private void anyPublicMethodReturningCallable() {
    }

    @Pointcut("(anyRestControllerAnnotated() || anyControllerAnnotated()) && anyPublicMethodReturningCallable()")
    private void anyControllerOrRestControllerWithPublicAsyncMethod() {
    }

    @Pointcut("execution(public org.springframework.web.context.request.async.WebAsyncTask *(..))")
    private void anyPublicMethodReturningWebAsyncTask() {
    }

    @Pointcut("(anyRestControllerAnnotated() || anyControllerAnnotated()) && anyPublicMethodReturningWebAsyncTask()")
    private void anyControllerOrRestControllerWithPublicWebAsyncTaskMethod() {
    }

    @Around("anyControllerOrRestController()")
    public Object wrapControllerOrRestController(ProceedingJoinPoint pjp) throws Throwable {
        return invoke(pjp);
    }

    @Around("anyControllerOrRestControllerWithPublicAsyncMethod()")
    @SuppressWarnings("unchecked")
    public Object wrapWithCorrelationId(ProceedingJoinPoint pjp) throws Throwable {
        return invokeReturningCallable(pjp);
    }

    @Around("anyControllerOrRestControllerWithPublicWebAsyncTaskMethod()")
    public Object wrapWebAsyncTaskWithCorrelationId(ProceedingJoinPoint pjp) throws Throwable {
        return invokeReturningWebAsyncTask(pjp);
    }

}
