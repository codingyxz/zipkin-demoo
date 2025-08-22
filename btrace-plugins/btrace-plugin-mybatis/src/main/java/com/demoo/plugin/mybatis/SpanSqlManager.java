package com.demoo.plugin.mybatis;

import com.demoo.plugin.utils.manager.AbstractSpanManager;

/**
 * @Description TODO
 * @Date 2025-06-27
 * @Created by Yolo
 */
public class SpanSqlManager extends AbstractSpanManager<SqlSpanDecorator> {

    public SpanSqlManager() {
        super(true);
    }

    public SpanSqlManager(boolean useDefaultDecorator) {
        super(useDefaultDecorator);
    }

    @Override
    public SqlSpanDecorator getDefaultDecorator() {
        return SqlSpanDecorator.DEFAULT_DECORATOR;
    }

}
