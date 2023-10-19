/*
 * Copyright (c) 2001-2017 GuaHao.com Corporation Limited.
 *  All rights reserved.
 *  This software is the confidential and proprietary information of GuaHao Company.
 *  ("Confidential Information").
 *  You shall not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered into with GuaHao.com.
 *
 */

package com.demoo.plugin.utils.manager;

import java.util.List;

/**
 * 管理span的装饰器
 *
 */
public interface SpanManager<T> {

    <T> T getDefaultDecorator();

    List<T> getDecorators();

    void addDecorator(T decorator);

    void addDecorator(int index, T decorator);

    void addDecorators(List<T> decorators);

    void removeDecorator(T decorators);

    void clearDecorators();
}
