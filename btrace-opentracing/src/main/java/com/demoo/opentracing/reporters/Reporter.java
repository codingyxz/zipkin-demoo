package com.demoo.opentracing.reporters;

import io.opentracing.Span;

/**
 * 发送者接口，用于将finish的span发送到采集服务器
 *
 */
public interface Reporter {

    void report(Span span);

    void close();
}
