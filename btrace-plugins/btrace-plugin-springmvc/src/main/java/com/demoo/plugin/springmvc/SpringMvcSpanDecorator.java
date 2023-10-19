package com.demoo.plugin.springmvc;

import com.demoo.opentracing.Constants;
import com.demoo.plugin.utils.decorator.SpanDecorator;
import com.demoo.plugin.utils.utils.RequestOperateUtil;
import com.demoo.plugin.utils.utils.SpanUtils;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * @author zhxy
 * @Date 2021/6/29 8:48 下午
 */
public interface SpringMvcSpanDecorator extends SpanDecorator<Method, Object> {

    String HTTP_COMPONENT = "http";
    String CONTROLLER_METHOD_NAME = "controller.method";
    String CONTROLLER_METHOD_PARAM = "method.param";


    void onPreHandle(HttpServletRequest httpServletRequest, Object handle, Span span);

    void onAfterCompletion(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Object handle,
            Exception ex,
            Span span);

    void onAfterConcurrentHandlingStarted(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Object handle,
            Span span);

    /**
     * 异步请求错误
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @param exception
     * @param span
     */
    void onAsyncError(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Throwable exception,
            Span span);

    /**
     * 异步请求超时
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @param timeout
     * @param span
     */
    void onAsyncTimeout(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            long timeout,
            Span span);

    SpringMvcSpanDecorator DEFAULT_DECORATOR = new SpringMvcSpanDecorator() {
        @Override
        public void onPreHandle(HttpServletRequest httpServletRequest, Object handle, Span span) {
            try {

                SpanUtils.setSpanToLogParameter(span);
                String methodName = null;
                if (handle instanceof HandlerMethod) {
                    methodName = ControllerUtils.requestMapping((HandlerMethod) handle);
                    span.setOperationName(methodName);
                }
                if (RequestOperateUtil.needTraceMethod(methodName)) {
                    this.setParam(httpServletRequest, span);
                }
                String sceneId;
                if ((sceneId = RequestOperateUtil.needSceneBaggage(methodName)) != null) {
                    span.setBaggageItem(Constants.Baggage.SCENE, sceneId);
                }

                SpanUtils.setRequestBaggage(httpServletRequest, span);
                Tags.COMPONENT.set(span, HTTP_COMPONENT);
                span.setTag(
                        CONTROLLER_METHOD_PARAM,
                        "[" + httpServletRequest.getMethod() + "]"
                                + ControllerUtils.name(handle));
                Tags.HTTP_METHOD.set(span, httpServletRequest.getMethod());
                Tags.HTTP_URL.set(span, httpServletRequest.getRequestURL().toString());

            } catch (Throwable throwable) {

            }
        }

        @Override
        public void onAfterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                      Object handle, Exception ex, Span span) {

            SpanUtils.setSpanToLogParameter(span);
            if (ex != null) {
                SpanUtils.logsForException(ex, span);
            }
        }

        @Override
        public void onAfterConcurrentHandlingStarted(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                                     Object handle, Span span) {
            SpanUtils.setSpanToLogParameter(span);
        }

        @Override
        public void onAsyncError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                 Throwable exception, Span span) {
            SpanUtils.logsForException(exception, span);
        }

        @Override
        public void onAsyncTimeout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                   long timeout, Span span) {
            SpanUtils.logsForTimeout(span, timeout);
        }

        @Override
        public void onBefore(Method target, Object[] args, Span span) {
            SpanUtils.setSpanToLogParameter(span);
        }

        @Override
        public void onAfter(Method target, Object[] args, Object result, Span span) {
            SpanUtils.setSpanToLogParameter(span);
        }

        @Override
        public void onException(Method target, Object[] args, Throwable throwable, Span span) {
            SpanUtils.setSpanToLogParameter(span);
            if (throwable != null) {
                SpanUtils.logsForException(throwable, span);
            }
        }

        private void setParam(HttpServletRequest httpServletRequest, Span span) {
            try {
                StringBuffer buffer = new StringBuffer();
                Map<String, String[]> paramMap = httpServletRequest.getParameterMap();
                if (paramMap != null && paramMap.size() > 0) {
                    for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                        if (entry.getValue() != null && entry.getValue().length > 0) {
                            String[] values = entry.getValue();
                            StringBuffer stringBuffer = new StringBuffer();
                            for (String str : values) {
                                stringBuffer.append(str + ",");
                            }
                            buffer.append(
                                    entry.getKey()
                                            + ":" + stringBuffer.toString().substring(0, stringBuffer.length() - 1)
                                            + " ");
                        }
                    }
                    span.setTag(CONTROLLER_METHOD_PARAM, buffer.toString().substring(0, buffer.length()));
                }
            } catch (Exception e) {

            }
        }
    };

    class ControllerUtils {

        private static final String DOT = ".";
        private static final String SLASH = "/";
        private static final String EMPTY = "";

        public static String name(Object handler) {
            String className = className(handler);
            String methodName = methodName(handler);
            return className + DOT + methodName;
        }

        public static String className(Object handler) {
            return handler instanceof HandlerMethod
                    ? ((HandlerMethod) handler).getBeanType().getSimpleName()
                    : null;
        }

        public static String methodName(Object handler) {
            return handler instanceof HandlerMethod
                    ? ((HandlerMethod) handler).getMethod().getName()
                    : null;
        }

        public static String requestMapping(HandlerMethod handler) {
            RequestMapping methodRequestMapping = handler.getMethodAnnotation(RequestMapping.class);
            RequestMapping classRequestMapping = handler.getBeanType().getAnnotation(RequestMapping.class);

            String[] methodMappings = methodRequestMapping == null ? null : methodRequestMapping.value();
            String[] classMappings = classRequestMapping == null ? null : classRequestMapping.value();

            String methodUri =
                    methodMappings == null
                            ? EMPTY
                            : (methodMappings.length == 1
                            ? methodMappings[0]
                            : Arrays.toString(methodMappings));
            String classUri =
                    classMappings == null
                            ? EMPTY
                            : (classMappings.length == 1
                            ? classMappings[0]
                            : Arrays.toString(classMappings));
            if (EMPTY.equals(methodUri) && EMPTY.equals(classUri)) {
                return name(handler);
            }
            if (EMPTY.equals(classUri)) {
                return methodUri;
            } else {
                String uri = SLASH + classUri + SLASH + methodUri;
                return uri.replace("//", "/");
            }

        }

    }

}
