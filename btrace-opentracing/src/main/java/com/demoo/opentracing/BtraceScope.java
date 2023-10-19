package com.demoo.opentracing;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.ThreadLocalScope;
import io.opentracing.util.ThreadLocalScopeManager;

/**
 * @author zhxy
 * @Date 2021/6/27 2:50 下午
 */
public class BtraceScope implements Scope {
    private final BtraceScopeManager scopeManager;
    private final Span wrapped;
    private final BtraceScope toRestore;

    BtraceScope(BtraceScopeManager scopeManager, Span wrapped) {
        this.scopeManager = scopeManager;
        this.wrapped = wrapped;
        this.toRestore = (BtraceScope)scopeManager.tlsScope.get();
        scopeManager.tlsScope.set(this);
    }

    @Override
    public void close() {
        if (this.scopeManager.tlsScope.get() == this) {
            this.scopeManager.tlsScope.set(this.toRestore);
        }
    }

    Span span() {
        return this.wrapped;
    }
}
