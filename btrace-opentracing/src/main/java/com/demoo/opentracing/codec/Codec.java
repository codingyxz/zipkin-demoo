package com.demoo.opentracing.codec;


import com.demoo.opentracing.BtraceSpan;
/**
 * proto span 编码接口
 */
public interface Codec {

    void maxSpanByteSize(int maxSpanByteSize);

    int add(BtraceSpan btraceSpan);

    int byteSize();

    int spansCount();

    byte[] encode();

    void reset();

    String getMediaType();

    Object getRawData();
}
