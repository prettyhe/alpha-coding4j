package com.alpha.coding.common.http.filter;

import java.util.function.Function;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.http.HttpUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * HeadersCORSFilter
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class HeadersCORSFilter extends AbstractEnhancerFilter {

    private Integer maxAge = 3600;
    private String allowMethods = "POST, GET, PUT, PATCH, DELETE, OPTIONS";
    private Function<String, String> allowOrigin = origin -> origin != null ? origin : "*";

    @Override
    public void postInit(FilterConfig filterConfig) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        final String maxAge = filterConfig.getInitParameter("maxAge");
        if (maxAge != null && maxAge.length() > 0) {
            this.maxAge = Integer.valueOf(maxAge.trim());
        }
        final String allowMethods = filterConfig.getInitParameter("allowMethods");
        if (allowMethods != null && allowMethods.length() > 0) {
            this.allowMethods = allowMethods;
        }
    }

    @Override
    public Tuple<ServletRequest, ServletResponse> beforeFilter(ServletRequest servletRequest,
                                                               ServletResponse servletResponse) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        final String domain = HttpUtils.getDomain(request);
        final String origin = request.getHeader("origin");
        if (log.isDebugEnabled()) {
            log.debug("get domain: {}, origin: {}", domain, origin);
        }
        response.setHeader("Access-Control-Allow-Origin", allowOrigin.apply(origin));
        response.setHeader("Access-Control-Allow-Methods", allowMethods);
        response.setHeader("Access-Control-Max-Age", String.valueOf(maxAge));
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        return new Tuple<>(request, response);
    }

    @Override
    public void afterFilter(ServletRequest servletRequest, ServletResponse servletResponse) {

    }

    @Override
    public void destroy() {

    }
}
