package com.demoo.opentracing.property;

/**
 * 管理属性监听器
 * @author zhxy
 * @Date 2021/6/28 8:50 下午
 */
public interface BtraceProperty<T> {

    void addListener(PropertyListener<T> listener);

    void removeListener(PropertyListener<T> listener);

    boolean updateValue(T newValue);
}
