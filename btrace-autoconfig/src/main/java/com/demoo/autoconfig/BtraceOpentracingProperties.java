package com.demoo.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zhxy
 * @Date 2021/6/28 8:13 下午
 */

@ConfigurationProperties(prefix = "btrace")
@Validated
public class BtraceOpentracingProperties {
    public static final String SENDER_TYPE_HTTP = "http";
    public static final String SENDER_TYPE_KAFKA = "kafka";


    @NotNull
    private String name;
    private boolean traceId128Bit = true;
    @Valid
    private Sender sender;
    private Sign sign = new Sign();

    public String getName() {
        return name;
    }

    public BtraceOpentracingProperties setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isTraceId128Bit() {
        return traceId128Bit;
    }

    public BtraceOpentracingProperties setTraceId128Bit(boolean traceId128Bit) {
        this.traceId128Bit = traceId128Bit;
        return this;
    }

    public Sender getSender() {
        return sender;
    }

    public BtraceOpentracingProperties setSender(Sender sender) {
        this.sender = sender;
        return this;
    }

    public Sign getSign() {
        return sign;
    }

    public BtraceOpentracingProperties setSign(Sign sign) {
        this.sign = sign;
        return this;
    }

    public static class Sender implements Serializable {
        private static final long serialVersionUID = 2670081757174390690L;
        private String type;
        private String address;
        private String topic;

        public String getType() {
            return type;
        }

        public Sender setType(String type) {
            this.type = type;
            return this;
        }

        public String getAddress() {
            return address;
        }

        public Sender setAddress(String address) {
            this.address = address;
            return this;
        }

        public String getTopic() {
            return topic;
        }

        public Sender setTopic(String topic) {
            this.topic = topic;
            return this;
        }
    }

    public static class Sign implements Serializable {
        private String appkey;
        private String appsecret;

        public String getAppkey() {
            return appkey;
        }

        public Sign setAppkey(String appkey) {
            this.appkey = appkey;
            return this;
        }

        public String getAppsecret() {
            return appsecret;
        }

        public Sign setAppsecret(String appsecret) {
            this.appsecret = appsecret;
            return this;
        }
    }
}
