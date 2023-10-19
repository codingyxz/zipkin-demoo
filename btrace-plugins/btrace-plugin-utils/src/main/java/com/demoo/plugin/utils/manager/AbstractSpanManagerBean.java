package com.demoo.plugin.utils.manager;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.List;

/**
 * 抽象管理者 span
 *
 */
public abstract class AbstractSpanManagerBean<M, D> implements FactoryBean, InitializingBean {

    protected M spanManager;

    protected boolean useDefaultDecorator = true;

    protected List<D> decorators = Collections.emptyList();

    public void setUseDefaultDecorator(boolean useDefaultDecorator) {
        this.useDefaultDecorator = useDefaultDecorator;
    }

    public void setDecorators(List<D> decorators) {
        this.decorators = decorators;
    }

    @Override
    public Object getObject() throws Exception {
        return spanManager;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public abstract Class<?> getObjectType();

    @Override
    public abstract void afterPropertiesSet();
}
