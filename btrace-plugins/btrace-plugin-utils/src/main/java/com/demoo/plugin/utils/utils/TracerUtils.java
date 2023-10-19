package com.demoo.plugin.utils.utils;

import org.slf4j.MDC;


public class TracerUtils {
    public static String injectTraceInfo(String message) {
        String traceInfos = MDC.get(SpanUtils.INJECT_SPAN_NAME);
        if (traceInfos != null) {
            String logMessage = TracerUtils.removeAllLast(message, System.getProperty("line.separator"));
            message = logMessage + " " + traceInfos + System.getProperty("line.separator");
        }
        return message;
    }

    public static String removeAllLast(String str, String removeStr) {
        int lastIndex;
        while ((lastIndex = str.lastIndexOf(removeStr)) > 0 && lastIndex + removeStr.length() == str.length()) {
            str = str.substring(0, lastIndex);
        }
        return str;
    }
}
