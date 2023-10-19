package com.demoo.plugin.spring.service.beans;

import com.demoo.plugin.spring.service.SpanSpringServiceManager;
import com.demoo.plugin.spring.service.SpringServiceSpanDecorator;
import com.demoo.plugin.utils.manager.AbstractSpanManagerBean;

/**
 * @author zhxy
 * @Date 2021/6/29 7:51 下午
 */
public class SpanSpringServiceManagerBean extends AbstractSpanManagerBean<SpanSpringServiceManager, SpringServiceSpanDecorator> {

    @Override
    public Class<?> getObjectType() {
        return SpanSpringServiceManager.class;
    }

    @Override
    public void afterPropertiesSet() {
        spanManager = new SpanSpringServiceManager(useDefaultDecorator);
        spanManager.addDecorators(decorators);
    }
}
