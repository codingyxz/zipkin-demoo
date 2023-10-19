package com.demoo.plugin.utils.utils;

/**
 *
 * 常量类
 *
 * @author zhxy
 * @Date 2021/6/30 4:57 下午
 */
public class ConstUtils {

    /**
     * servlet 创建的 span 存入到attribute的这个key中
     */
    public static final String SERVLET_SPAN = "btrace.plugin.servlet.span";

    /**
     * 标识是否需要tracing
     */
    public static final String IS_SKIP_TRACING = "btrace.plugin.servlet.isSkipTracing";

    /**
     * 使用一个key从servletContext中获取spanServletManager
     */
    public static final String SPAN_SERVLET_MANAGER = "btrace.plugin.servlet.spanServletManager";

}
