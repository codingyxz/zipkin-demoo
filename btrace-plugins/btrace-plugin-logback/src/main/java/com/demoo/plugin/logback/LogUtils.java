package com.demoo.plugin.logback;

import com.demoo.plugin.utils.utils.SpanUtils;
import io.opentracing.Span;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 日志工具类
 * @Date 2025-06-27
 * @Created by Yolo
 */
public class LogUtils {

    private static final String ERROR_CODE = "error.code";

    public static void logsForException(Throwable throwable, Span span) {

        // 处理指定异常信息（比如处理参数缺失异常）
        if (throwable instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException exception = (MissingServletRequestParameterException) throwable;
            Map<String, String> fields = new HashMap<String, String>(2);
            fields.put(SpanUtils.ERROR_OBJECT, throwable.getClass().getName());
            fields.put(SpanUtils.MESSAGE, exception.getMessage() + "-" + exception.getParameterName());
            span.log(fields);
        } else {
            SpanUtils.logsForException(throwable, span);
        }
    }

}
