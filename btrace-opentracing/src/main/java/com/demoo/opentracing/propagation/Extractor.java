package com.demoo.opentracing.propagation;

import com.demoo.opentracing.BtraceSpanContext;

public interface Extractor<T> {
    BtraceSpanContext extract(T carrier);
}
