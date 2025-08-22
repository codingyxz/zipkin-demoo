package com.demoo.plugin.mybatis;

import com.demoo.plugin.mybatis.utils.SqlSpanUtils;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @Description TODO
 * @Date 2025-06-27
 * @Created by Yolo
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})})
public class CustomInterceptor implements Interceptor {

    private static final Logger LOGGER = Logger.getLogger(CustomInterceptor.class.getName());

    private Tracer tracer;

    private SpanSqlManager spanSqlManager;

    public CustomInterceptor(Tracer tracer, SpanSqlManager spanSqlManager) {
        this.spanSqlManager = spanSqlManager;
        this.tracer = tracer;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (tracer == null || null == spanSqlManager || tracer.activeSpan() == null) {
            return invocation.proceed();
        }
        // 获取span
        Span activeSpan = tracer.activeSpan();
        Object returnValue = invocation.proceed();
        Connection connection = SqlSpanUtils.getConnection(invocation);
        String methodName = "invoke database";
        try {
            if (connection != null) {
                SpanContext spanContext = activeSpan.context();
                // 创建activeSpan
                activeSpan = spanContext == null
                        ? tracer.buildSpan(methodName).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start()
                        : tracer.buildSpan(methodName).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).asChildOf(spanContext).start();
            }
            for (SqlSpanDecorator spanDecorator : spanSqlManager.getDecorators()) {
                spanDecorator.onBefore(invocation, activeSpan, connection);
            }
        } catch (Exception e) {
            for (SqlSpanDecorator spanDecorator : spanSqlManager.getDecorators()) {
                spanDecorator.onException(invocation, e, activeSpan, connection);
            }
        } finally {
            if (activeSpan != null && connection == null) {
                activeSpan.finish();
            }
        }
        return returnValue;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
