package com.alpha.coding.common.http.filter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.constant.Keys;
import com.alpha.coding.bo.trace.HalfTraceIdGenerator;
import com.alpha.coding.bo.trace.TimestampUUIDTraceIdGenerator;
import com.alpha.coding.bo.trace.TraceIdGenerator;
import com.alpha.coding.bo.trace.UUIDTraceIdGenerator;
import com.alpha.coding.common.utils.StringUtils;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * TraceAgentFilter
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Data
@Accessors(chain = true)
public class TraceAgentFilter extends AbstractEnhancerFilter {

    private TraceIdGenerator traceIdGenerator = TimestampUUIDTraceIdGenerator.getInstance();

    private TraceIdGenerator tailTraceIdGenerator = UUIDTraceIdGenerator.getInstance();

    @Override
    public void postInit(FilterConfig filterConfig) throws ServletException {
        try {
            final String traceIdGenerator = filterConfig.getInitParameter("traceIdGenerator");
            if (StringUtils.isNotBlank(traceIdGenerator)) {
                this.traceIdGenerator = (TraceIdGenerator) loadClass(traceIdGenerator).newInstance();
            }
        } catch (Exception e) {
            log.warn("Init TraceAgentFilter Exception", e);
        }
    }

    @Override
    public Tuple<ServletRequest, ServletResponse> beforeFilter(ServletRequest servletRequest,
                                                               ServletResponse servletResponse) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String oldTraceId = null;
        if (MDC.getMDCAdapter() != null) {
            oldTraceId = MDC.get(Keys.TRACE_ID);
        }
        if (oldTraceId == null) {
            oldTraceId = request.getHeader(Keys.HEADER_TRACE);
        }
        String newTraceId = oldTraceId == null ? traceIdGenerator.traceId()
                : new HalfTraceIdGenerator(tailTraceIdGenerator).halfTrace(oldTraceId);
        if (log.isDebugEnabled()) {
            log.debug("NewTraceId derive: {} ==> {}", oldTraceId, newTraceId);
        }
        if (MDC.getMDCAdapter() != null) {
            MDC.put(Keys.TRACE_ID, newTraceId);
        }
        MapThreadLocalAdaptor.put(Keys.TRACE_ID, newTraceId);
        return new Tuple<>(request, response);
    }

    @Override
    public void afterFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
        if (MDC.getMDCAdapter() != null) {
            MDC.remove(Keys.TRACE_ID);
        }
        MapThreadLocalAdaptor.remove(Keys.TRACE_ID);
    }

    @Override
    public void destroy() {

    }

}
