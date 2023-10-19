package com.demoo.opentracing;

import io.opentracing.SpanContext;

import java.io.Serializable;

/**
 * 引用关系
 *
 */
public class Reference implements Serializable {

    private static final long serialVersionUID = -1616507416100656247L;
    private SpanContext spanContext;
    private String type;

    public Reference(SpanContext spanContext, String type) {
        this.spanContext = spanContext;
        this.type = type;
    }

    public SpanContext getSpanContext() {
        return spanContext;
    }

    public void setSpanContext(SpanContext spanContext) {
        this.spanContext = spanContext;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reference reference = (Reference) o;

        if (spanContext != null ? !spanContext.equals(reference.spanContext) : reference.spanContext != null)
            return false;
        return type != null ? type.equals(reference.type) : reference.type == null;
    }

    @Override
    public int hashCode() {
        int result = spanContext != null ? spanContext.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

}
