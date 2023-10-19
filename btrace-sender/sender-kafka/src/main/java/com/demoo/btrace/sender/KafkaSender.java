package com.demoo.btrace.sender;

import com.demoo.opentracing.BtraceSpan;
import com.demoo.opentracing.codec.Codec;
import com.demoo.opentracing.internal.Lazy;
import com.demoo.opentracing.internal.Utils;
import com.demoo.opentracing.senders.AbstractBuilder;
import com.demoo.opentracing.senders.Sender;
import io.opentracing.Span;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author zhxy
 * @Date 2021/6/28 12:17 下午
 */
public class KafkaSender extends Lazy<KafkaProducer<byte[], byte[]>> implements Sender {

    static final Logger LOGGER = Logger.getLogger(KafkaSender.class.getName());

    public static final String DEFAULT_TOPIC = "btrace";


    private int maxSpansBytes;
    private Codec codec;
    private Properties properties;
    private String topic;

    public KafkaSender(int maxSpansBytes, Properties properties, String topic, Codec codec) {
        this.maxSpansBytes = maxSpansBytes;
        this.codec = codec;
        this.properties = properties;
        this.topic = topic;
    }

    public static Builder newBuilder(String bootstrapServers){
        Properties properties = new Properties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,ByteArraySerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG,"0");
        return new Builder(bootstrapServers)
                .properties(properties)
                .topic(DEFAULT_TOPIC)
                .overrides(Collections.EMPTY_MAP);
    }

    public static class Builder extends AbstractBuilder {
        Properties properties = new Properties();
        String topic;

        Builder(String bootstrapServers) {
            super(bootstrapServers);
            this.properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    Utils.checkNotNull(bootstrapServers, "bootstrapServers"));
        }

        Builder properties(Properties properties) {
            if (properties != null) {
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    this.properties.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }
            return this;
        }

        /**
         * Topic btrace spans will be send to. Defaults to "btrace"
         */
        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public final Builder overrides(Map<String, String> overrides) {
            properties.putAll(Utils.checkNotNull(overrides, "overrides"));
            return this;
        }

        public Sender build() {
            return new KafkaSender(maxSpansByteSize, properties, topic, codec);
        }
    }

    @Override
    protected KafkaProducer<byte[], byte[]> compute() {
        return new KafkaProducer<byte[], byte[]>(properties);
    }

    @Override
    public int append(Span span) {
        long byteSize = this.codec.add(((BtraceSpan) span));

//        if (byteSize <= maxSpansBytes) {
//            return 0;
//        }
        return flush();
    }

    @Override
    public int flush() {
        if(codec.byteSize() == 0){
            return 0;
        }
        try {
            final byte[] message = codec.encode();
            get().send(new ProducerRecord<>(topic, message), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        LOGGER.log(Level.WARNING, String.format("Could not send %d spans, kafka send error: %s",
                                codec.spansCount(), exception.getMessage()), exception);
                    }
                }
            });

            return codec.byteSize();
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, String.format("Could not send %d spans, kafka send error: %s",
                    codec.spansCount(), e.getMessage()), e);
            if (e instanceof Error) throw (Error) e;
        } finally {
            codec.reset();
        }
        return 0;
    }

    @Override
    public int close() {
        return flush();
    }

    @Override
    public void send(String topic, String json) {

        byte[] messages = json.getBytes();
        get().send(new ProducerRecord<>(topic, messages), new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    LOGGER.log(Level.WARNING, String.format("Could not send %d spans, kafka send error: %s",
                            codec.spansCount(), exception.getMessage()), exception);
                }
            }
        });
    }
}
