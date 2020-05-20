package com.alpha.coding.common.http.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * FilterEnhancer
 *
 * @version 1.0
 * Date: 2020-01-15
 */
public interface FilterEnhancer {

    void before(ServletRequest servletRequest, ServletResponse servletResponse);

    void after(ServletRequest servletRequest, ServletResponse servletResponse);

}
