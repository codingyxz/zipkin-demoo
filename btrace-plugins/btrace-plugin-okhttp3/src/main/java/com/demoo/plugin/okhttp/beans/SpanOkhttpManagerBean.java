package com.demoo.plugin.okhttp.beans;

import com.demoo.plugin.okhttp.OkhttpSpanDecorator;
import com.demoo.plugin.okhttp.SpanOkhttpManager;
import com.demoo.plugin.utils.manager.AbstractSpanManagerBean;


public class SpanOkhttpManagerBean extends AbstractSpanManagerBean<SpanOkhttpManager, OkhttpSpanDecorator> {
    @Override
    public Class<?> getObjectType() {
        return SpanOkhttpManager.class;
    }

    @Override
    public void afterPropertiesSet() {
        spanManager = new SpanOkhttpManager(useDefaultDecorator);
        spanManager.addDecorators(decorators);
    }
}
