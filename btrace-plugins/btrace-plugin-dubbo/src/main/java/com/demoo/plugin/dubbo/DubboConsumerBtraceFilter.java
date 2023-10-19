package com.demoo.plugin.dubbo;

import io.opentracing.Scope;
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

/**
 * dubbo消费者关于opentracing的拦截实现
 *
 * @author zhxy
 * @Date 2021/7/4 2:49 下午
 */


@Activate(group = CommonConstants.CONSUMER, order = -50000)
public class DubboConsumerBtraceFilter extends DubboBtraceBaseFilter implements Filter {


    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if (tracer == null || spanDubboManager == null || tracer instanceof NoopTracer) {
            return invoker.invoke(invocation);
        }
        if (isDubboMonitor(invocation)) {
            return invoker.invoke(invocation);
        }

        Span parentSpan = tracer.scopeManager().activeSpan();
        SpanContext spanContext = parentSpan == null ? null : parentSpan.context();

        String operationName = buildOperationName(invoker, invocation);
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(operationName)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

        Span span = spanContext == null ? spanBuilder.start() : spanBuilder.asChildOf(spanContext).start();
        try (Scope scope = tracer.scopeManager().activate(span)) {

            tracer.inject(span.context(), Format.Builtin.TEXT_MAP, new TextMapAdapter(invocation.getAttachments()));
            onBefore(invocation, span);
            Result result = invoker.invoke(invocation);
            onAfter(invocation, result, span);
            return result;
        } catch (Exception ex) {
            onException(invocation, invocation.getArguments(), ex, span);
            throw ex;
        } finally {
            span.finish();
        }
    }
}
