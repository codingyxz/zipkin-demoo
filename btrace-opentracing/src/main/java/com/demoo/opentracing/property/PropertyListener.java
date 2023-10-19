package com.demoo.opentracing.property;

/**
 *
 * 属性监听器
 * @author zhxy
 * @Date 2021/6/28 8:50 下午
 */
public interface PropertyListener<T> {

    void configUpdate(T value);

    void configLoad(T value);
}
