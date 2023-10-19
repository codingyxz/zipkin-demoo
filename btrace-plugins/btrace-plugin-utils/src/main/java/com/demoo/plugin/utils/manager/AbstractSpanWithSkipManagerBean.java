package com.demoo.plugin.utils.manager;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 抽象管理者 span with skip
 */
public abstract class AbstractSpanWithSkipManagerBean<M, D> implements FactoryBean, InitializingBean {

    protected M spanManager;
    protected boolean useDefaultDecorator = true;
    protected boolean useDefaultSkipPattern = true;

    protected List<D> decorators = Collections.emptyList();
    protected List<Pattern> skipPatterns = Collections.emptyList();

    public void setUseDefaultDecorator(boolean useDefaultDecorator) {
        this.useDefaultDecorator = useDefaultDecorator;
    }

    public void setUseDefaultSkipPattern(boolean useDefaultSkipPattern) {
        this.useDefaultSkipPattern = useDefaultSkipPattern;
    }

    public void setDecorators(List<D> decorators) {
        this.decorators = decorators;
    }

    public void setSkipPatterns(List<Pattern> skipPatterns) {
        this.skipPatterns = skipPatterns;
    }

    public List<D> getDecorators() {
        return decorators;
    }

    public List<Pattern> getSkipPatterns() {
        return skipPatterns;
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
