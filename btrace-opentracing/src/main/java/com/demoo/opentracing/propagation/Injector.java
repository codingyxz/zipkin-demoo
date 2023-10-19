
package com.demoo.opentracing.propagation;


import com.demoo.opentracing.BtraceSpanContext;

public interface Injector<T> {
  void inject(BtraceSpanContext spanContext, T carrier);
}
