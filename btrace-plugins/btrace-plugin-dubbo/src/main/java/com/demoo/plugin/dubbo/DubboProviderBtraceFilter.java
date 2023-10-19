package com.demoo.plugin.dubbo;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.logging.Logger;

/**
 * dubbo提供者关于opentracing的拦截实现
 *
 * @author zhxy
 * @Date 2021/7/4 11:28 上午
 */

@Activate(group = CommonConstants.PROVIDER, order = -50000)
public class DubboProviderBtraceFilter extends DubboBtraceBaseFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(DubboProviderBtraceFilter.class.getName());

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if (tracer == null || spanDubboManager == null || tracer instanceof NoopTracer) {
            return invoker.invoke(invocation);
        }

        if (isDubboMonitor(invocation)) {
            return invoker.invoke(invocation);
        }

        Span span = null;
        try {
            span = extractTraceInfo(invoker, invocation, tracer);
            if (span == null) {
                return invoker.invoke(invocation);
            }
            onBefore(invocation, span);
            Result result = invoker.invoke(invocation);
            onAfter(invocation, result, span);
            return result;
        } catch (RpcException rpc) {
            onException(invocation, invocation.getArguments(), rpc, span);
            throw rpc;
        } finally {
            if (span != null) {
                span.finish();
            }
        }
    }


    protected Span extractTraceInfo(Invoker<?> invoker, Invocation invocation, Tracer tracer) {
        try {
            String operationName = buildOperationName(invoker, invocation);
            SpanContext spanContext = tracer.extract(Format.Builtin.TEXT_MAP,
                    new TextMapAdapter(invocation.getAttachments()));

            Tracer.SpanBuilder span = tracer.buildSpan(operationName)
                    .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
            if (spanContext != null) {
                span.asChildOf(spanContext);
            }
            return span.start();

        } catch (Exception e) {
            LOGGER.throwing(DubboProviderBtraceFilter.class.getName(), "extractTraceInfo", e);
            return null;
        }
    }
}
