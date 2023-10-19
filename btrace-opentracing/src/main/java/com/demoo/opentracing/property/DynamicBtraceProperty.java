package com.demoo.opentracing.property;

import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author zhxy
 * @Date 2021/6/28 8:53 下午
 */
public class DynamicBtraceProperty<T> implements BtraceProperty<T> {

    private static final Logger LOGGER = Logger.getLogger(DynamicBtraceProperty.class.getName());
    protected Set<PropertyListener<T>> listeners = Collections.synchronizedSet(new HashSet<PropertyListener<T>>());
    private T value = null;

    public DynamicBtraceProperty() {
    }

    public DynamicBtraceProperty(T value) {
        super();
        this.value = value;
    }

    @Override
    public void addListener(PropertyListener<T> listener) {
        listeners.add(listener);
        listener.configLoad(value);
    }

    @Override
    public void removeListener(PropertyListener<T> listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean updateValue(T newValue) {
        if (isEqual(value, newValue)) {
            return false;
        }
        LOGGER.info("[DynamicBtraceProperty] Config will be updated to: " + JSON.toJSONString(value));

        value = newValue;
        for (PropertyListener<T> listener : listeners) {
            listener.configUpdate(newValue);
        }
        return false;
    }

    private boolean isEqual(T oldValue, T newValue) {
        if (oldValue == null && newValue == null) {
            return true;
        }
        if (oldValue == null) {
            return false;
        }
        return oldValue.equals(newValue);
    }
}
