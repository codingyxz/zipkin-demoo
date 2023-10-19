package com.demoo.opentracing.senders;


import com.demoo.opentracing.codec.Codec;

/**
 * 抽象builder
 * Created by freeway on 2017/9/23.
 */
public abstract class AbstractBuilder {

    public static final int DEFAULT_MAX_PER_SPAN_BYTES = 1024*100;
    public static final int DEFAULT_MAX_SPANS_BYTES = 1024*512;

    protected String endpoint;
    protected Codec codec;
    protected int maxSpanByteSize = DEFAULT_MAX_PER_SPAN_BYTES;
    protected int maxSpansByteSize = DEFAULT_MAX_SPANS_BYTES;

    public AbstractBuilder(String endpoint) {
        this.endpoint = endpoint;
    }

    public AbstractBuilder codec(Codec codec) {
        codec.maxSpanByteSize(maxSpanByteSize);
        this.codec = codec;
        return this;
    }

    public AbstractBuilder maxSpanByteSize(int maxSpanByteSize) {
        this.maxSpanByteSize = maxSpanByteSize;
        if (this.codec!=null) {
            this.codec.maxSpanByteSize(maxSpanByteSize);
        }
        return this;
    }

    public AbstractBuilder maxSpansByteSize(int maxSpansByteSize) {
        this.maxSpansByteSize = maxSpansByteSize;
        return this;
    }

}
