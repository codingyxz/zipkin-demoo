package com.demoo.plugin.dubbo;

import com.demoo.plugin.utils.manager.AbstractSpanManager;

/**
 * @author zhxy
 * @Date 2021/7/4 10:49 上午
 */
public class SpanDubboManager extends AbstractSpanManager<DubboSpanDecorator> {


    public SpanDubboManager() {
    }

    public SpanDubboManager(boolean useDefaultDecorator) {
        super(useDefaultDecorator);
    }

    @Override
    public DubboSpanDecorator getDefaultDecorator() {
        return DubboSpanDecorator.DEFAULT_DECORATOR;
    }
}
