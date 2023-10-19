package com.demoo.plugin.dubbo;

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.apache.dubbo.rpc.Constants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

import java.util.List;

/**
 * @author zhxy
 * @Date 2021/7/4 10:48 上午
 */
public class DubboBtraceBaseFilter {

    protected Tracer tracer;
    protected SpanDubboManager spanDubboManager;
    protected static final String DUBBO_MONITOR_METHOD = "collect";
    protected static final String ALI_DUBBO_MONITOR_COLLECT_PARAMETER = "com.alibaba.dubbo.common.URL";
    protected static final String APA_DUBBO_MONITOR_COLLECT_PARAMETER = "org.apache.dubbo.common.URL";
    private static final String TRUE = "true";


    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public void setSpanDubboManager(SpanDubboManager spanDubboManager) {
        this.spanDubboManager = spanDubboManager;
    }

    public List<DubboSpanDecorator> getDubboSpanDecorator() {
        if (this.spanDubboManager == null) {
            this.spanDubboManager = new SpanDubboManager();
        }
        return this.spanDubboManager.getDecorators();
    }


    protected void onBefore(Invocation invocation, Span span) {
        for (DubboSpanDecorator dubboSpanDecorator : getDubboSpanDecorator()) {
            dubboSpanDecorator.onBefore(invocation, invocation.getArguments(), span);
        }
    }

    protected void onAfter(Invocation invocation, Result result, Span span) {
        for (DubboSpanDecorator dubboSpanDecorator : getDubboSpanDecorator()) {
            dubboSpanDecorator.onAfter(invocation, invocation.getArguments(), result, span);
        }
    }

    protected void onException(Invocation invocation, Object[] args, Throwable e, Span span) {
        for (DubboSpanDecorator dubboSpanDecorator : getDubboSpanDecorator()) {
            dubboSpanDecorator.onException(invocation, args, e, span);
        }
    }


    public static final String SPLITTER = ",";
    public static final String DOT = ".";

    private String join(Class<?>[] classes, String splitter) {
        StringBuilder builder = new StringBuilder("");
        if (classes == null || classes.length == 0) {
            return builder.toString();
        }

        for (Class<?> clazz : classes) {
            builder.append(clazz.getSimpleName()).append(splitter);
        }
        return builder.substring(0, builder.length() - 1);
    }


    protected String buildOperationName(Invoker<?> invoker, Invocation invocation) {
        String generic = invoker.getUrl().getParameter(Constants.GENERIC_KEY);
        if (TRUE.equals(generic)) {
            String service = invoker.getUrl().getParameter("interface");
            StringBuilder operationName = getSimpleServiceName(service);
            if (invocation.getArguments() != null && invocation.getArguments().length > 0) {
                operationName.append(".").append(invocation.getArguments()[0]);
            }
            return operationName.toString();
        }
        String arg = join(invocation.getParameterTypes(), SPLITTER);
        return invoker.getInterface().getSimpleName() + DOT + invocation.getMethodName() + "(" + arg + ")";
    }


    protected static StringBuilder getSimpleServiceName(String service) {
        StringBuilder builder = new StringBuilder();
        if (service != null && !"".equals(service)) {
            int len = service.length();
            if (service.lastIndexOf(".") > 0 && (service.lastIndexOf(".") + 1) < len) {
                builder.append(service.substring(service.lastIndexOf(".") + 1, len));
            }
        }
        return builder;
    }

    protected static boolean isDubboMonitor(Invocation invocation) {
        if (invocation == null) {
            return false;
        }
        if (DUBBO_MONITOR_METHOD.equals(invocation.getMethodName())
                && invocation.getParameterTypes().length == 1
                && APA_DUBBO_MONITOR_COLLECT_PARAMETER.equals(invocation.getParameterTypes()[0].getName())
                && ALI_DUBBO_MONITOR_COLLECT_PARAMETER.equals(invocation.getParameterTypes()[0].getName())) {
            return true;
        }
        return false;
    }
}
