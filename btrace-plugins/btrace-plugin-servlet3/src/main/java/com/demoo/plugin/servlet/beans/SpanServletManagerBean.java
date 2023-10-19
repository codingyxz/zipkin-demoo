package com.demoo.plugin.servlet.beans;

import com.demoo.plugin.servlet.ServletFilterSpanDecorator;
import com.demoo.plugin.servlet.SpanServletManager;
import com.demoo.plugin.utils.manager.AbstractSpanWithSkipManagerBean;


/**
 * @author zhxy
 * @Date 2021/6/30 3:49 下午
 */
public class SpanServletManagerBean extends AbstractSpanWithSkipManagerBean<SpanServletManager, ServletFilterSpanDecorator> {

    @Override
    public Class<?> getObjectType() {
        return SpanServletManager.class;
    }

    @Override
    public void afterPropertiesSet() {
        assert getSkipPatterns() != null;
        assert getDecorators() != null;
        spanManager = new SpanServletManager(useDefaultDecorator, useDefaultDecorator);
        spanManager.addSkipPatterns(getSkipPatterns());
        spanManager.addDecorators(getDecorators());
    }
}
