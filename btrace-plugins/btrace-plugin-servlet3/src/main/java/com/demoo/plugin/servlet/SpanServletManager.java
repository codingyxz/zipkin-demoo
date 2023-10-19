package com.demoo.plugin.servlet;

import com.demoo.plugin.utils.manager.AbstractSpanWithSkipManager;

import java.util.Collections;

/**
 * @author zhxy
 * @Date 2021/6/30 1:25 下午
 */
public class SpanServletManager extends AbstractSpanWithSkipManager<ServletFilterSpanDecorator> {


    public SpanServletManager() {
        this(true,true);
    }

    public SpanServletManager(boolean useDefaultDecorator,boolean useDefaultSkipPatterns) {
        super(useDefaultDecorator);
        if(useDefaultSkipPatterns){
            skipPatterns = Collections.singletonList(DEFAULT_SKIP_PATTERN);
        }else {
            skipPatterns = Collections.emptyList();
        }
    }

    @Override
    public ServletFilterSpanDecorator getDefaultDecorator() {
        return ServletFilterSpanDecorator.DEFAULT_DECORATOR;
    }
}
