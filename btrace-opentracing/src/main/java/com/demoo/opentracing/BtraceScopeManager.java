package com.demoo.opentracing;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.util.ThreadLocalScope;

/**
 * @author zhxy
 * @Date 2021/6/27 2:47 下午
 */
public class BtraceScopeManager implements ScopeManager {

    final ThreadLocal<BtraceScope> tlsScope = new ThreadLocal<>();

    @Override
    public Scope activate(Span span) {
        return new BtraceScope(this, span);
    }

    @Override
    public Span activeSpan() {
        BtraceScope scope = (BtraceScope)this.tlsScope.get();
        return scope == null ? null : scope.span();
    }
}
