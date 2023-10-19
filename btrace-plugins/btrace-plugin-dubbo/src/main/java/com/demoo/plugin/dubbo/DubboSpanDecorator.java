package com.demoo.plugin.dubbo;

import com.demoo.plugin.utils.decorator.SpanDecorator;
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
                    generic = target.getInvoker().getUrl().getParameter(Constants.GENERIC_KEY);
                }



            } catch (Exception ex) {

            }
        }

        @Override
        public void onAfter(Invocation target, Object[] args, Result result, Span span) {

        }

        @Override
        public void onException(Invocation target, Object[] args, Throwable throwable, Span span) {

        }
    };
}
