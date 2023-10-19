package com.demoo.plugin.utils.utils;

import com.demoo.opentracing.BtraceConfigManager;
import io.opentracing.Span;
import javax.servlet.ServletRequest;
import java.util.Map;

/**
 * 对请求对象request的attribute进行set get remove等操作
 *
 */
public class RequestOperateUtil {

    /**
     * 获取
     *
     * @param request
     * @return
     */
    public static Span getServletSpan(ServletRequest request) {
        return (Span) request.getAttribute(ConstUtils.SERVLET_SPAN);
    }

    /**
     * 设置
     *
     * @param request
     * @param span
     */
    public static void setServletSpan(ServletRequest request, Span span) {
        request.setAttribute(ConstUtils.SERVLET_SPAN, span);
    }

    /**
     * 移除
     *
     * @param request
     */
    public static void removeServletSpan(ServletRequest request) {
        request.removeAttribute(ConstUtils.SERVLET_SPAN);
    }

    /**
     * 是否需要记录请求参数
     *
     * @param methodName
     * @return
     */
    public static boolean needTraceMethod(String methodName) {
        String[] configNames = BtraceConfigManager.getConfig().getTraceMethodsArray();
        for (String configName : configNames) {
            if (configName.equals(methodName)) {
                return true;
            }
        }

        return false;
    }

    public static String needSceneBaggage(String methodName) {
        Map<String, String> methodMap = BtraceConfigManager.getConfig().getSceneBaggageMap();
        for (String configName : methodMap.keySet()) {
            if (configName.equals(methodName)) {
                return methodMap.get(configName);
            }
        }

        return null;
    }
}

