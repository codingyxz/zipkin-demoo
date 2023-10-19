/*
 * Copyright (c) 2001-2017 GuaHao.com Corporation Limited.
 *  All rights reserved.
 *  This software is the confidential and proprietary information of GuaHao Company.
 *  ("Confidential Information").
 *  You shall not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered into with GuaHao.com.
 *
 */

package com.demoo.spring;

import com.demoo.btrace.sender.KafkaSender;
import com.demoo.btrace.sender.OkHttpSender;
import com.demoo.opentracing.BtraceTracer;
import com.demoo.opentracing.codec.BtracePbCodec;
import com.demoo.opentracing.codec.JsonCodec;
import com.demoo.opentracing.codec.ZipkinThriftCodec;
import com.demoo.opentracing.reporters.QueueReporter;
import com.demoo.opentracing.samplers.Sampler;
import com.demoo.opentracing.senders.LogSender;
import com.demoo.opentracing.senders.Sender;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.logging.Logger;

public class TracerBean implements FactoryBean<Tracer>, InitializingBean {
    private static final Logger logger = Logger.getLogger(TracerBean.class.getName());
    private Tracer tracer;

    private String name;
    private String signAppsecret;
    private String signAppkey;
    private String senderType;
    private String senderAddress;
    private String senderTopic;
    private boolean enabled = true;
    private boolean traceId128Bit = true;
    public static final String TOPIC_ZIPKIN = "zipkin";
    public static final String TOPIC_BTRACE = "btrace";
    public static final String TOPIC_JSON = "btrace_json";
    private static final String STANDARD_NAME = "STANDARD_NAME";

    private Sampler sampler = Sampler.ALWAYS_SAMPLE;

    public void setName(String name) {
        this.name = name;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public void setSenderTopic(String senderTopic) {
        this.senderTopic = senderTopic;
    }

    public void setTraceId128Bit(boolean traceId128Bit) {
        this.traceId128Bit = traceId128Bit;
    }

    public void setSampler(Sampler sampler) {
        this.sampler = sampler;
    }

    public void setSignAppsecret(String signAppsecret) {
        this.signAppsecret = signAppsecret;
    }

    public void setSignAppkey(String signAppkey) {
        this.signAppkey = signAppkey;
    }

    @Override
    public Tracer getObject() throws Exception {
        return tracer;
    }

    @Override
    public Class<?> getObjectType() {
        return Tracer.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String appName = this.getAppName(name);

        Sender sender;
        if (!enabled) {
            tracer = NoopTracerFactory.create();
            return;
        }
        if ("http".equals(senderType)) {
            OkHttpSender.Builder builder = OkHttpSender.newBuilder(senderAddress);
            if (TOPIC_JSON.equals(senderTopic)) {
                builder.withAppKey((signAppkey == null || "".equals(signAppkey)) ? "" : signAppkey);
                builder.withAppSecret(
                        (signAppsecret == null || "".equals(signAppsecret)) ? "" : signAppsecret);
                builder.codec(new JsonCodec());
            } else {
                builder.codec(new BtracePbCodec());
            }
            sender = builder.build();
        } else if ("kafka10".equals(senderType)) {
            KafkaSender.Builder builder = KafkaSender.newBuilder(senderAddress);
            if (TOPIC_ZIPKIN.equals(senderTopic)) {
                builder.codec(new ZipkinThriftCodec());
                builder.topic(TOPIC_ZIPKIN);
            } else {
                builder.codec(new BtracePbCodec());
                builder.topic(TOPIC_BTRACE);
            }
            sender = builder.build();
        } else {
            sender = new LogSender();
        }

        tracer =
                BtraceTracer.newBuilder(appName, QueueReporter.newBuilder(sender).build(), sampler)
                        .traceId128Bit(traceId128Bit)
                        .build();
    }

    private String getAppName(String name) {
        try {
            String serviceName = System.getenv(STANDARD_NAME);
            if (serviceName != null && !"".equals(serviceName)) {
                return serviceName;
            }
        } catch (Exception e) {

        }
        return name;
    }
}
