package com.demoo.autoconfig;

import com.demoo.btrace.sender.KafkaSender;
import com.demoo.btrace.sender.OkHttpSender;
import com.demoo.opentracing.BtraceConfig;
import com.demoo.opentracing.BtraceConfigManager;
import com.demoo.opentracing.BtraceTracer;
import com.demoo.opentracing.codec.BtracePbCodec;
import com.demoo.opentracing.codec.Codec;
import com.demoo.opentracing.codec.JsonCodec;
import com.demoo.opentracing.codec.ZipkinThriftCodec;
import com.demoo.opentracing.reporters.QueueReporter;
import com.demoo.opentracing.reporters.Reporter;
import com.demoo.opentracing.samplers.Sampler;
import com.demoo.opentracing.senders.LogSender;
import com.demoo.opentracing.senders.Sender;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;


@Configuration
// btrace.name 这个参数没找到说明btrace的配置一定存在问题，不启动trace
@ConditionalOnProperty(prefix = "btrace", name = "name")
@EnableConfigurationProperties(com.demoo.autoconfig.BtraceOpentracingProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 3)
public class TraceAutoConfiguration {

    public static final String TOPIC_ZIPKIN = "zipkin";
    public static final String TOPIC_BTRACE = "btrace_flink";
    public static final String TOPIC_JSON = "btrace_json";
    private static final String STANDARD_NAME = "STANDARD_NAME";
    private static boolean enabled = true;
    private static Sampler sampler = Sampler.ALWAYS_SAMPLE;

    @Autowired
    BtraceOpentracingProperties btraceOpentracingProperties;

    @Bean
    @ConditionalOnMissingBean
    public Codec defaultTraceSpanCodec() {
        if (TOPIC_ZIPKIN.equals(btraceOpentracingProperties.getSender().getTopic())) {
            return new ZipkinThriftCodec();
        } else if (TOPIC_JSON.equals(btraceOpentracingProperties.getSender().getTopic())) {
            return new JsonCodec();
        }
        return new BtracePbCodec();
//        return new JsonCodec();
    }

    @Bean
    @ConditionalOnMissingBean
    public Sender defaultTraceSender(Codec codec) {
        String senderType = btraceOpentracingProperties.getSender().getType();
        String senderAddress = btraceOpentracingProperties.getSender().getAddress();
        String senderTopic = btraceOpentracingProperties.getSender().getTopic();
        String appkey = btraceOpentracingProperties.getSign().getAppkey();
        String appsecret = btraceOpentracingProperties.getSign().getAppsecret();

        if (btraceOpentracingProperties.SENDER_TYPE_HTTP.equals(senderType)) {
            OkHttpSender.Builder builder = OkHttpSender.newBuilder(senderAddress);
            builder.withAppKey((appkey == null || "".equals(appkey)) ? "" : appkey);
            builder.withAppSecret((appsecret == null || "".equals(appsecret)) ? "" : appsecret);
            builder.codec(codec);
            return builder.build();
        } else if (com.demoo.autoconfig.BtraceOpentracingProperties.SENDER_TYPE_KAFKA.equals(senderType)) {
            KafkaSender.Builder builder = KafkaSender.newBuilder(senderAddress);
            builder.codec(codec);
            if (TOPIC_ZIPKIN.equals(senderTopic)) {
                builder.topic(TOPIC_ZIPKIN);
            } else if (TOPIC_JSON.equals(senderTopic)) {
                builder.topic(TOPIC_JSON);
            } else {
                builder.topic(TOPIC_BTRACE);
            }
            return builder.build();
        } else {
            return new LogSender();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public Reporter defaultReporter(Sender sender) {
        return QueueReporter.newBuilder(sender).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracer tracer(Reporter reporter) {
        String appName = this.getAppName();
//        GconfigCfgLoader.load(appName);
        BtraceConfig btraceConfig = BtraceConfigManager.getConfig();
        if (btraceConfig != null) {
            enabled = btraceConfig.isEnabled();
            sampler = btraceConfig.getSampler();
        }

        if (!enabled) {
            return NoopTracerFactory.create();
        }
        return BtraceTracer.newBuilder(appName, reporter, sampler)
                .traceId128Bit(btraceOpentracingProperties.isTraceId128Bit()).build();
    }

    private String getAppName() {
        try {
            String serviceName = System.getenv(STANDARD_NAME);
            if (serviceName != null && !"".equals(serviceName)) {
                return serviceName;
            }
        } catch (Exception e) {

        }
        return btraceOpentracingProperties.getName();
    }

}
