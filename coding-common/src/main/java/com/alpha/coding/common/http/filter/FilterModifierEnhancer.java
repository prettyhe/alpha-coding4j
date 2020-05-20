package com.alpha.coding.common.http.filter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.alpha.coding.bo.base.Tuple;

/**
 * FilterModifierEnhancer
 *
 * @version 1.0
 * Date: 2020-01-15
 */
public interface FilterModifierEnhancer {

    void postInit(FilterConfig filterConfig) throws ServletException;

    Tuple<ServletRequest, ServletResponse> beforeFilter(ServletRequest servletRequest, ServletResponse servletResponse);

    void afterFilter(ServletRequest servletRequest, ServletResponse servletResponse);

}
