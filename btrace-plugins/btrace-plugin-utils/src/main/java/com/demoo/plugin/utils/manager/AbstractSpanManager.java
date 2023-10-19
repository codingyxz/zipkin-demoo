package com.demoo.plugin.utils.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 抽象span管理者
 *
 */
public abstract class AbstractSpanManager<T> implements SpanManager<T> {

    protected List<T> decorators;
    
    public AbstractSpanManager() {
        this(true);
    }

    public AbstractSpanManager(boolean useDefaultDecorator) {
        if (useDefaultDecorator) {
            this.decorators = Collections.singletonList(getDefaultDecorator());
        } else {
            this.decorators = Collections.emptyList();
        }
    }

    @Override
    public List<T> getDecorators() {
        return decorators;
    }

    @Override
    public void addDecorator(T decorator) {
        if (decorator == null) {
            return;
        }
        List<T> newDecorators = new ArrayList<T>(decorators);
        newDecorators.add(decorator);
        decorators = Collections.unmodifiableList(newDecorators);
    }

    @Override
    public void addDecorator(int index, T decorator) {
        if (decorator == null) {
            return;
        }
        List<T> newDecorators = new ArrayList<T>(decorators);
        newDecorators.add(index, decorator);
        decorators = Collections.unmodifiableList(newDecorators);
    }

    @Override
    public void addDecorators(List<T> decorators) {
        if (decorators == null || decorators.size() == 0) {
            return;
        }
        List<T> newDecorators = new ArrayList<T>(this.decorators);
        newDecorators.addAll(decorators);
        this.decorators = Collections.unmodifiableList(newDecorators);
    }

    @Override
    public void removeDecorator(T decorator) {
        if (decorator == null) {
            return;
        }
        List<T> newDecorators = new ArrayList<T>(this.decorators);
        newDecorators.remove(decorator);
        this.decorators = Collections.unmodifiableList(newDecorators);
    }

    @Override
    public void clearDecorators() {
        this.decorators = Collections.emptyList();
    }
}
