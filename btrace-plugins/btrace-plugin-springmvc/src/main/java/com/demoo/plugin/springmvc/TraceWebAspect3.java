package com.demoo.plugin.springmvc;

import io.opentracing.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * 限定spring 3.x 使用
 *
 * @author zhxy
 * @Date 2021/6/30 11:08 上午
 */
@Aspect
public class TraceWebAspect3 extends TraceWebAspect {
    public TraceWebAspect3(Tracer tracer) {
        super(tracer);
    }

    public TraceWebAspect3(Tracer tracer, SpanSpringMvcManager spanSpringMvcManager) {
        super(tracer, spanSpringMvcManager);
    }

    @Pointcut("@within(org.springframework.stereotype.Controller)")
    private void anyControllerAnnotated() {
    }

    @Around("anyControllerAnnotated()")
    public Object wrapControllerOrRestController(ProceedingJoinPoint pjp) throws Throwable {
        return invoke(pjp);
    }
}
