package com.demoo.plugin.mybatis;

import com.demoo.plugin.mybatis.utils.SqlSpanUtils;
import com.demoo.plugin.utils.utils.SpanUtils;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import org.apache.ibatis.plugin.Invocation;

import java.sql.Connection;

/**
 * @Description sql span装饰器
 * @Date 2025-06-27
 * @Created by Yolo
 */
public interface SqlSpanDecorator {

    String DB_IP = "db.ip";

    String DB_NAME = "db.name";

    String DB = "dataBase";

    /**
     * 请求出现异常调用
     *
     * @param throwable
     * @param span
     */
    void onException(Invocation invocation, Throwable throwable, Span span, Connection connection);

    void onBefore(Invocation invocation, Span span, Connection connection);


    SqlSpanDecorator DEFAULT_DECORATOR = new SqlSpanDecorator() {

        @Override
        public void onException(Invocation invocation, Throwable throwable, Span span, Connection connection) {
            try {
                if (invocation != null && throwable != null && span != null) {
                    Tags.DB_STATEMENT.set(span, SqlSpanUtils.getSql(SqlSpanUtils.getStatementHandler(invocation)));
                    if (connection != null) {
                        Tags.DB_TYPE.set(span, SqlSpanUtils.databaseType(connection));
                        String instance = SqlSpanUtils.getInstant(connection);
                        span.setTag(DB_IP, SqlSpanUtils.getDbIp(instance));
                        span.setTag(DB_NAME, SqlSpanUtils.getDbName(instance));
                    }
                    SpanUtils.logsForException(throwable, span);
                }
            } catch (Exception e) {

            }
        }

        @Override
        public void onBefore(Invocation invocation, Span span, Connection connection) {
            try {
                if (invocation != null && span != null) {
                    Tags.DB_STATEMENT.set(span, SqlSpanUtils.getSql(SqlSpanUtils.getStatementHandler(invocation)));
                    // prepare拦截时不为null
                    if (connection != null) {
                        Tags.COMPONENT.set(span, DB);
                        Tags.DB_TYPE.set(span, SqlSpanUtils.databaseType(connection));
                        String instance = SqlSpanUtils.getInstant(connection);
                        span.setTag(DB_IP, SqlSpanUtils.getDbIp(instance));
                        span.setTag(DB_NAME, SqlSpanUtils.getDbName(instance));
                    } else {
                        String daoMethod = SqlSpanUtils.getDaoMethod(invocation);
                        if (daoMethod != null && daoMethod.length() > 0) {
                            span.setOperationName(daoMethod);
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
    };

}
