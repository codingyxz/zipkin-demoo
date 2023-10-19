package com.demoo.plugin.springmvc.beans;

import com.demoo.plugin.springmvc.SpanSpringMvcManager;
import com.demoo.plugin.springmvc.SpringMvcSpanDecorator;
import com.demoo.plugin.utils.manager.AbstractSpanWithSkipManagerBean;

/**
 * @author zhxy
 * @Date 2021/6/30 6:07 下午
 */
public class SpanSpringMvcManagerBean extends AbstractSpanWithSkipManagerBean<SpanSpringMvcManager, SpringMvcSpanDecorator> {


    @Override
    public Class<?> getObjectType() {
        return SpanSpringMvcManager.class;
    }

    @Override
    public void afterPropertiesSet() {
        spanManager = new SpanSpringMvcManager(useDefaultDecorator,useDefaultSkipPattern);
        spanManager.addSkipPatterns(getSkipPatterns());
        spanManager.addDecorators(decorators);
    }
}
