package com.demoo.opentracing;

/**
 * 常量类
 * Created by freeway on 2017/9/15.
 */
public interface Constants {

    /**
     * 参考 https://github.com/opentracing/specification/blob/master/semantic_conventions.md
     */
    interface LogFields {
        String EVENT = "event";
        String PAYLOAD = "payload";
        String MESSAGE = "message";
        String ERROR_KIND = "error.kind";
        String ERROR_OBJECT = "error.object";
        String STACK = "stack";
    }

    interface LogEvent {
        String CLIENT_RECEIVE = "cr";
        String CLIENT_SEND = "cs";
        String SERVER_SEND = "ss";
        String SERVER_RECEIVE = "sr";
        String MESSAGE_SEND = "ms";
        String MESSAGE_RECEIVE = "mr";
    }

    interface Kind {
        String SERVER = "SERVER";
        String CLIENT = "CLIENT";
        String PRODUCER = "PRODUCER";
        String CONSUMER = "CONSUMER";
    }

    interface Baggage {
        String SCENE = "scene";
        String CALL_TEST = "callTestId";
    }

    String GTRACE_GCONFIG_FILE_NAME = "gtrace.gconfig.properties";

    String DEBUG_ID_HEADER_KEY = "X-GT-D-";

    /**
     * Key used to store serialized span context representation
     */
    String SPAN_CONTEXT_KEY = "X-GT-C-";

    /**
     * Key prefix used for baggage items
     */
    String BAGGAGE_KEY_PREFIX = "X-GT-B-";


    String GTRACE_CLIENT_VERSION_TAG_KEY = "gtrace.version";

}
