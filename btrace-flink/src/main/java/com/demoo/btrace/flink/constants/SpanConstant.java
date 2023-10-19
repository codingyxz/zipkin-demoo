package com.demoo.btrace.flink.constants;

/**
 * @author zhxy
 * @Date 2021/7/1 4:51 下午
 */
public class SpanConstant {


    /**
     * server端span属性名
     */
    public static class Field {
        /**
         * tag里面的key
         */
        public static final String HTTP_URL = "http.url";

    }

    /**
     * span属性中的一些固定值
     */
    public static class Value {
        // server类型的span
        public static final String KIND_SERVER = "SERVER";
        // client类型的span
        public static final String KIND_CLIENT = "CLIENT";

        //事件Key
        public static final String EVENT_KEY = "event";
        public static final String ERROR_KEY = "error";
        public static final String UNKNOWN_KEY = "unknown";
        public static final String BAGGAGE_PREFIX = "baggage.";
        public static final String ERROR_OBJECT_KEY = "error.object";
        //时间value
        public static final String CLIENT_RECEIVE = "cr";
        public static final String CLIENT_SEND = "cs";
        public static final String SERVER_SEND = "ss";
        public static final String SERVER_RECEIVE = "sr";

        /**
         * dubbo监控的操作名
         */
        public static final String DUBBO_MONITOR_NAME = "MonitorService.collect(URL)";

    }
}
