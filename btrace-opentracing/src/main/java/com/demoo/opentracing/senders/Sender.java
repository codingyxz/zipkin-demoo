package com.demoo.opentracing.senders;

import io.opentracing.Span;

/**
 * 发送者
 */
public interface Sender {
    int append(Span span);

    int flush();

    int close();

    void send(String topic,String json);
}
