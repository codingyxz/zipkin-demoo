package com.demoo.plugin.spring.service;

import com.demoo.plugin.utils.utils.SpanUtils;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracer;
import io.opentracing.tag.Tags;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * @author zhxy
 * @Date 2021/6/29 7:19 下午
 */

@Aspect
@Order(Integer.MAX_VALUE)
public class TraceServiceAspect {

    protected Tracer tracer;

    protected SpanSpringServiceManager spanManager;

    public TraceServiceAspect(Tracer tracer) {
        this(tracer,new SpanSpringServiceManager());
    }

    public TraceServiceAspect(Tracer tracer, SpanSpringServiceManager spanManager) {
        this.tracer = tracer;
        this.spanManager = spanManager;
    }

    @Pointcut("@within(org.springframework.stereotype.Service)")
    private void serviceAspect(){}

    @Around("serviceAspect()")
    public Object afterThrowing(ProceedingJoinPoint joinPoint) throws Throwable{
        if(tracer == null || tracer instanceof NoopTracer){
            return joinPoint.proceed();
        }
        Span parentSpan = tracer.scopeManager().activeSpan();

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        Span span = null;
        try{
            return joinPoint.proceed();
        } catch (Throwable throwable){
            // 组装新的span
            span = parentSpan == null ? tracer.buildSpan(SpanUtils.buildOperationName(method))
                                .withTag(Tags.SPAN_KIND.getKey(),Tags.SPAN_KIND_SERVER)
                                .start() :
                                        tracer.buildSpan(SpanUtils.buildOperationName(method))
                                .asChildOf(parentSpan)
                                .withTag(Tags.SPAN_KIND.getKey(),Tags.SPAN_KIND_SERVER)
                                .start();
            try(Scope scope = tracer.scopeManager().activate(span)){
                Object[] args = joinPoint.getArgs();
                for(SpringServiceSpanDecorator decorator : spanManager.getDecorators()){
                    decorator.onException(method,args,throwable,span);
                }
                throw throwable;
            }finally {
                span.finish();
            }

        }
    }
}
