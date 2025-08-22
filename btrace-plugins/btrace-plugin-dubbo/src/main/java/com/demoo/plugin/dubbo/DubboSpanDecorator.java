package com.demoo.plugin.dubbo;

import com.alibaba.fastjson.JSON;
import com.demoo.plugin.utils.decorator.SpanDecorator;
import com.demoo.plugin.utils.utils.RequestOperateUtil;
import com.demoo.plugin.utils.utils.SpanUtils;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import org.apache.dubbo.rpc.Constants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;

/**
 * @author zhxy
 * @Date 2021/7/2 2:48 下午
 */
public interface DubboSpanDecorator extends SpanDecorator<Invocation, Result> {

    String COMPONENT = "dubbo";
    String DUBBO_URL = COMPONENT + ".url";
    String PARAM = "param";
    String TRUE = "true";

    DubboSpanDecorator DEFAULT_DECORATOR = new DubboSpanDecorator() {
        @Override
        public void onBefore(Invocation target, Object[] args, Span span) {
            try {
                SpanUtils.setSpanToLogParameter(span);
                Tags.COMPONENT.set(span, COMPONENT);
                String generic = null;
                if (target.getInvoker() != null
                        && target.getInvoker().getUrl() != null
                        && !"".equals(target.getInvoker().getUrl())) {
                    generic =
                            target.getInvoker()
                                    .getUrl()
                                    .getParameter(Constants.GENERIC_KEY);
                }

                StringBuilder operationName =
                        DubboBtraceBaseFilter.getSimpleServiceName(
                                target.getAttachment("interface"));
                operationName.append(".").append(target.getMethodName());
                String sceneId;
                if ((sceneId =
                        RequestOperateUtil.needSceneBaggage(
                                operationName.toString()))
                        != null) {
                    span.setBaggageItem(
                            com.demoo.opentracing.Constants.Baggage.SCENE, sceneId);
                }

                boolean needTraceMethod =
                        RequestOperateUtil.needTraceMethod(operationName.toString());
                    /*    if (!needTraceMethod) {
                            return;
                        }*/

                if (TRUE.equals(generic)) {
                    this.buildGeneric(target, span);
                } else {
                    this.buildNonGeneric(target, span);
                }
            } catch (Throwable throwable) {
            }
        }

        @Override
        public void onAfter(
                Invocation target, Object[] args, Result result, Span span) {
            SpanUtils.setSpanToLogParameter(span);
            if (result.hasException()) {
                onException(target, args, result.getException(), span);
            }
        }

        @Override
        public void onException(
                Invocation target, Object[] args, Throwable throwable, Span span) {
            SpanUtils.setSpanToLogParameter(span);
            SpanUtils.logsForException(throwable, span);
            span.setTag(DUBBO_URL, target.getInvoker().getUrl().toString());
        }

        private void buildNonGeneric(Invocation invocation, Span span) {
            try {
                Object[] objects = invocation.getArguments();
                StringBuffer paramBuffer = new StringBuffer();
                if (objects != null && objects.length > 0) {
                    for (Object ob : objects) {
                        String paramName = ob.getClass().getSimpleName();
                        if (paramName != null && !"".equals(paramName)) {
                            String paramValue = JSON.toJSONString(ob);
                            paramBuffer.append(paramName + ":" + paramValue + "  ");
                        }
                    }
                    span.setTag(PARAM, paramBuffer.toString());
                }
            } catch (Exception e) {

            }
        }

        private void buildGeneric(Invocation invocation, Span span) {
            try {
                Object[] objects = invocation.getArguments();
                StringBuffer paramBuffer = new StringBuffer();
                if (objects != null && objects.length > 2) {
                    paramBuffer
                            .append("paramType:")
                            .append(JSON.toJSONString(objects[1]))
                            .append("\n");
                    paramBuffer.append("value").append(":").append(JSON.toJSONString(objects[2]));
                }
                span.setTag(PARAM, paramBuffer.toString());
            } catch (Exception e) {

            }
        }
    };
}
