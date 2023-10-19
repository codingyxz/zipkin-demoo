package com.demoo.opentracing.senders;

import com.demoo.opentracing.BtraceSpan;
import io.opentracing.Span;

import java.util.logging.Logger;

/**
 * 日志发送者
 * Created by freeway on 2017/9/23.
 */
public class LogSender implements Sender {

    static final Logger LOGGER = Logger.getLogger(LogSender.class.getName());

    @Override
    public int append(Span span) {
        LOGGER.info("append span:" +((BtraceSpan)span).getSpanData().toString());
        return 0;
    }

    @Override
    public int flush() {
        LOGGER.info("flush LogSender");
        return 0;
    }

    @Override
    public void send(String topic,String json) {
        LOGGER.info("send LogSender");
    }

    @Override
    public int close() {
        LOGGER.info("close LogSender");
        return 0;
    }
}
