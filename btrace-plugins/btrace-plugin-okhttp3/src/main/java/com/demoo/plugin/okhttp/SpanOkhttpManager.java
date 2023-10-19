package com.demoo.plugin.okhttp;

import com.demoo.plugin.utils.manager.AbstractSpanManager;

public class SpanOkhttpManager extends AbstractSpanManager<OkhttpSpanDecorator> {
    public SpanOkhttpManager() {
        super(true);
    }

    public SpanOkhttpManager(boolean useDefaultDecorator) {
        super(useDefaultDecorator);
    }

    @Override
    public OkhttpSpanDecorator getDefaultDecorator() {
        return OkhttpSpanDecorator.DEFAULT_DECORATOR;
    }
}
