package com.alpha.coding.common.http.filter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.function.common.Functions;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * TimeElapseFilter
 *
 * @version 1.0
 * Date: 2020-04-12
 */
@Slf4j
@Accessors(chain = true)
public class TimeElapseFilter extends AbstractEnhancerFilter {

    private static final String ELAPSED_TIME_ST_KEY = "_ELAPSED_TIME_ST_";
    private static final String ELAPSED_TIME_ST_NANOS_KEY = "_ELAPSED_TIME_ST_NANOS_";

    @Override
    public void postInit(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public Tuple<ServletRequest, ServletResponse> beforeFilter(ServletRequest servletRequest,
                                                               ServletResponse servletResponse) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        MapThreadLocalAdaptor.put(ELAPSED_TIME_ST_KEY, System.currentTimeMillis());
        MapThreadLocalAdaptor.put(ELAPSED_TIME_ST_NANOS_KEY, System.nanoTime());
        return Tuple.of(request, response);
    }

    @Override
    public void afterFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        final Long st = (Long) MapThreadLocalAdaptor.get(ELAPSED_TIME_ST_KEY);
        final Long stNanos = (Long) MapThreadLocalAdaptor.get(ELAPSED_TIME_ST_NANOS_KEY);
        long et = System.currentTimeMillis();
        if (st == null || stNanos == null) {
            log.info("invoke {},st=,et={},costTime=",
                    request.getRequestURI().substring(request.getContextPath().length()), getTimeStr(et));
        } else {
            log.info("invoke {},st={},et={},costTime={}",
                    request.getRequestURI().substring(request.getContextPath().length()),
                    getTimeStr(st), getTimeStr(et),
                    Functions.formatNanos.apply(System.nanoTime() - stNanos));
        }
        MapThreadLocalAdaptor.remove(ELAPSED_TIME_ST_KEY);
        MapThreadLocalAdaptor.remove(ELAPSED_TIME_ST_NANOS_KEY);
    }

    @Override
    public void destroy() {

    }

    private static String getTimeStr(Long logTime) {
        if (logTime == null) {
            return "";
        }
        String milliSecond = String.valueOf(logTime);
        return milliSecond.substring(0, milliSecond.length() - 3) + "."
                + milliSecond.substring(milliSecond.length() - 3);
    }

}
