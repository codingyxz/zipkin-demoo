package com.demoo.spring;

import com.demoo.btrace.sender.KafkaSender;
import com.demoo.btrace.sender.OkHttpSender;
import com.demoo.opentracing.BtraceTracer;
import com.demoo.opentracing.codec.Codec;
import com.demoo.opentracing.reporters.QueueReporter;
import com.demoo.opentracing.reporters.Reporter;
import com.demoo.opentracing.samplers.Sampler;
import com.demoo.opentracing.senders.LogSender;
import com.demoo.opentracing.senders.Sender;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Logger;

/**
 * @author zhxy
 * @Date 2021/6/28 12:00 下午
 */

//@Configuration
public class BtraceConfig {

    private static final Logger LOGGER = Logger.getLogger(BtraceConfig.class.getName());

    public static final String SENDER_TYPE_HTTP = "http";
    public static final String SENDER_TYPE_KAFKA10 = "kafka10";
    public static final String SENDER_TYPE_LOG = "log";

    @Value("${btrace.app_name}")
    private String name;

    @Value("${btrace.sampler}")
    private String sampler;

    @Value("${btrace.sender.type}")
    private String senderType;

    @Value("${btrace.sender.address}")
    private String senderAddress;

    //eg: btrace.sender.codec=com.demoo.opentracing.codec.ZipkinThriftCodec
    @Value("${btrace.sender.codec}")
    private String senderCodec;

    @Value("${btrace.sender.topic}")
    private String senderTopic;

    @Value("${btrace.traceId128Bit}")
    private String traceId128Bit;

    @Value("${btrace.enabled}")
    private boolean enable;

    @Bean
    public Tracer tracer() {
        boolean enabled = true;
        Sampler sampler = Sampler.ALWAYS_SAMPLE;

        if (!enable) {
            return NoopTracerFactory.create();
        }

        Sender sender = null;
        Codec codec = null;
        if (senderCodec != null) {
            try {
                codec = (Codec)Class.forName(senderCodec).newInstance();
            } catch (Exception e) {

            }
        }

        if (SENDER_TYPE_HTTP.equals(senderType)) {
            OkHttpSender.Builder builder = OkHttpSender.newBuilder(senderAddress);
            if (codec!=null) {
                builder.codec(codec);
            }
            sender = builder.build();
        } else if (SENDER_TYPE_KAFKA10.equals(senderType)) {
            KafkaSender.Builder builder = KafkaSender.newBuilder(senderAddress);
            if (senderTopic!=null) {
                builder.topic(senderTopic);
            }
            if (codec!=null) {
                builder.codec(codec);
            }
            sender = builder.build();
        } else if (SENDER_TYPE_LOG.equals(senderType)) {
            sender = new LogSender();
        }
        Reporter reporter = QueueReporter.newBuilder(sender).build();
        BtraceTracer.Builder tracerBuilder = BtraceTracer.newBuilder(name, reporter, sampler);
        if (traceId128Bit!=null) {
            tracerBuilder.traceId128Bit(Boolean.valueOf(traceId128Bit));
        }
        return tracerBuilder.build();
    }
}
