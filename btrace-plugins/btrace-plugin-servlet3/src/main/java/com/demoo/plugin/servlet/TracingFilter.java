package com.demoo.plugin.servlet;

import com.demoo.plugin.utils.utils.ConstUtils;
import com.demoo.plugin.utils.utils.RequestOperateUtil;
import com.demoo.plugin.utils.web.HttpServletRequestExtractAdapter;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author zhxy
 * @Date 2021/6/30 2:02 下午
 */
public class TracingFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(TracingFilter.class.getName());

    public static final int ORDER = Integer.MIN_VALUE + 5;
    private FilterConfig filterConfig;
    private Tracer tracer;
    private SpanServletManager spanServletManager;

    public TracingFilter(Tracer tracer) {
        this(tracer, new SpanServletManager());
    }

    public TracingFilter(Tracer tracer, SpanServletManager spanServletManager) {
        this.tracer = tracer;
        this.spanServletManager = spanServletManager;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        ServletContext servletContext = filterConfig.getServletContext();

        Object contextAttribute = servletContext.getAttribute(ConstUtils.SPAN_SERVLET_MANAGER);
        if (contextAttribute != null) {
            this.spanServletManager = (SpanServletManager) contextAttribute;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        if (isSkipTracing(httpRequest)) {
            filterChain.doFilter(httpRequest, httpResponse);
            return;
        }

        // 获取servlet当前的span,如果为异步请求的话，request里面会有span
        Span servletSpan = RequestOperateUtil.getServletSpan(httpRequest);
        // 如果为null，则创建一个span
        if (servletSpan == null) {
            // 尝试从header中提取span context
            SpanContext spanContext = tracer.extract(Format.Builtin.HTTP_HEADERS,
                    new HttpServletRequestExtractAdapter(httpRequest));
            // 创建新span
            servletSpan = spanContext == null ?
                    tracer.buildSpan(httpRequest.getRequestURI())
                            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                            .start() :
                    tracer.buildSpan(httpRequest.getRequestURI())
                            .asChildOf(spanContext)
                            .start();
            RequestOperateUtil.setServletSpan(httpRequest, servletSpan);
        }
        try (Scope scope = tracer.scopeManager().activate(servletSpan)) {
            for (ServletFilterSpanDecorator decorator : spanServletManager.getDecorators()) {
                decorator.onRequest(httpRequest, null, servletSpan);
            }
            filterChain.doFilter(httpRequest, httpResponse);
            for (ServletFilterSpanDecorator decorator : spanServletManager.getDecorators()) {
                decorator.onResponse(httpResponse, servletSpan);
            }
        } catch (IOException ex){
            for (ServletFilterSpanDecorator decorator : spanServletManager.getDecorators()) {
                decorator.onException(ex, servletSpan);
            }
            throw ex;
        }catch (ServletException ex) {
            for (ServletFilterSpanDecorator decorator : spanServletManager.getDecorators()) {
                decorator.onException(ex, servletSpan);
            }
            throw ex;
        } catch (RuntimeException ex) {
            for (ServletFilterSpanDecorator decorator : spanServletManager.getDecorators()) {
                decorator.onException(ex, servletSpan);
            }
            throw ex;
        } finally {
            RequestOperateUtil.removeServletSpan(httpRequest);
            servletSpan.finish();
        }

    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

    protected boolean isSkipTracing(HttpServletRequest httpServletRequest) {
        for (Pattern pattern : spanServletManager.getSkipPatterns()) {
            String url = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
            if (pattern.matcher(url).matches()) {
                httpServletRequest.setAttribute(ConstUtils.IS_SKIP_TRACING, true);
                return true;
            }
        }
        return false;
    }
}
