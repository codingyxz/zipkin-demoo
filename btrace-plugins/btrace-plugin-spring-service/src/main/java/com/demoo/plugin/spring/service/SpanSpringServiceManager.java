package com.demoo.plugin.spring.service;

import com.demoo.plugin.utils.manager.AbstractSpanManager;

/**
 * @author zhxy
 * @Date 2021/6/29 7:21 下午
 */
public class SpanSpringServiceManager extends AbstractSpanManager<SpringServiceSpanDecorator> {

    public SpanSpringServiceManager() {
        super(true);
    }

    public SpanSpringServiceManager(boolean useDefaultDecorator) {
        super(useDefaultDecorator);
    }

    @Override
    public SpringServiceSpanDecorator getDefaultDecorator() {
        return SpringServiceSpanDecorator.DEFAULT_DECORATOR;
    }
}
