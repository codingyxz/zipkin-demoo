package com.demoo.plugin.utils.utils;

import com.demoo.opentracing.BtraceSpanContext;
import com.demoo.opentracing.Constants;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.tag.Tags;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpanUtils {

    public static final String MESSAGE = "message";
    public static final String ERROR_OBJECT = "error.object";
    public static final String STACK = "stack";
    public static final String EVENT = "event";
    public static final String EVENT_TIMEOUT = "timeout";
    public static final String EMPTY = "";
    public static final String ERROR_CODE = "error.code";
    public static final String INJECT_SPAN_NAME = "inject_agent_span";

    /**
     * 并不标注error=true，表示这种异常是一种业务异常，便于后面定位问题
     *
     * @param throwable
     * @param span
     */
    public static void logsForMessageAndErrorObject(Throwable throwable, Span span) {
        span.log(getLogsForMessageAndErrorObject(throwable));
    }

    public static Map<String, String> getLogsForMessageAndErrorObject(Throwable throwable) {
        Map<String, String> fields = new HashMap<String, String>();
        if (throwable.getMessage() != null) {
            fields.put(MESSAGE, throwable.getMessage());
        }
        fields.put(ERROR_OBJECT, throwable.getClass().getName());
        return fields;
    }

    /**
     * 打印tag信息
     *
     * @param message
     * @param span
     * @param error
     */
    public static void logsForMessage(String message, Span span, boolean error) {
        if (error) {
            Tags.ERROR.set(span, Boolean.TRUE);
        }
        if (message != null && !message.trim().equals("")) {
            span.log(Collections.singletonMap(MESSAGE, message));
        }
    }

    /**
     * 将堆栈计入日志, 但并不标注error=true，表示这种异常是一种业务异常，便于后面定位问题
     *
     * @param throwable
     * @param span
     */
    public static void logsForStack(Throwable throwable, Span span) {
        span.log(getLogsForStack(throwable));
    }

    public static Map<String, String> getLogsForStack(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String stack = sw.toString();
        Map<String, String> fields = new HashMap<String, String>();
        fields.put(STACK, stack);
        return fields;
    }

    public static void logsForException(Throwable e, Span span) {
        try {
            if (e != null && span != null) {
                Tags.ERROR.set(span, Boolean.TRUE);
                logsForMessageAndErrorObject(e, span);
                logsForStack(e, span);
            }
        } catch (Throwable e1) {
            Tags.ERROR.set(span, Boolean.TRUE);
            logsForMessageAndErrorObject(e, span);
            logsForStack(e, span);
        }
    }

    public static void logsForTimeout(Span span, long timeout) {
        Tags.ERROR.set(span, Boolean.TRUE);
        Map<String, String> fields = new HashMap<String, String>();
        fields.put(EVENT, EVENT_TIMEOUT);
        fields.put(EVENT_TIMEOUT, String.valueOf(timeout));
        span.log(fields);
    }

    private static final String SPLITTER = ",";
    private static final String DOT = ".";

    private static String join(Class<?>[] classes, String splitter) {
        StringBuilder sb = new StringBuilder("");
        if (classes == null || classes.length == 0) {
            return EMPTY;
        }
        for (Class<?> clasz : classes) {
            sb.append(clasz.getSimpleName()).append(splitter);
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static String buildOperationName(Method method) {
        String args = join(method.getParameterTypes(), SPLITTER);
        return method.getDeclaringClass().getSimpleName()
                + DOT
                + method.getName()
                + "("
                + args
                + ")";
    }

    public static String buildMethodString(Method method) {
        String args = join(method.getParameterTypes(), SPLITTER);
        return method.getDeclaringClass().getName() + DOT + method.getName() + "(" + args + ")";
    }

    public static void setSpanToLogParameter(Span span) {
        if (span == null) {
            return;
        }
        SpanContext spanContext = span.context();
        if (spanContext == null) {
            return;
        }
        if (spanContext instanceof BtraceSpanContext) {
            BtraceSpanContext btraceSpanContext = (BtraceSpanContext) spanContext;
            try {
                MDC.put(
                        INJECT_SPAN_NAME,
                        String.format(
                                " [\"traceId\":\"%s\",\"spanId\":\"%s\"]",
                                btraceSpanContext.getTraceId(), btraceSpanContext.getSpanId()));
            } catch (Throwable ignored) {
            }
        }
    }

    public static void setRequestBaggage(HttpServletRequest httpServletRequest, Span span) {
        String callTestId = span.getBaggageItem(Constants.Baggage.CALL_TEST);
        if (callTestId != null && !"".equals(callTestId)) {
            return;
        }
        callTestId =
                httpServletRequest.getHeader(
                        Constants.BAGGAGE_KEY_PREFIX + Constants.Baggage.CALL_TEST);
        if (callTestId != null && !"".equals(callTestId)) {
            span.setBaggageItem(Constants.Baggage.CALL_TEST, callTestId);
        }
    }
}
