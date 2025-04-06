package com.alpha.coding.common.http.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.function.common.Predicates;
import com.alpha.coding.common.utils.ClassUtils;

import lombok.Setter;

/**
 * AbstractEnhancerFilter
 *
 * @version 1.0
 * Date: 2020-01-15
 */
public abstract class AbstractEnhancerFilter implements Filter, FilterModifierEnhancer {

    @Setter
    private List<FilterEnhancer> filterEnhancers;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        final String filterEnhancers = filterConfig.getInitParameter("filterEnhancers");
        if (Predicates.isNotBlankStr.test(filterEnhancers)) {
            this.filterEnhancers = new ArrayList<>();
            for (String name : filterEnhancers.split(",")) {
                try {
                    this.filterEnhancers.add((FilterEnhancer) ClassUtils.newInstance(loadClass(name)));
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
        }
        postInit(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final Tuple<ServletRequest, ServletResponse> tuple = beforeFilter(request, response);
        if (this.filterEnhancers != null && !this.filterEnhancers.isEmpty()) {
            this.filterEnhancers.forEach(p -> p.before(tuple.getF(), tuple.getS()));
        }
        try {
            chain.doFilter(tuple.getF(), tuple.getS());
        } finally {
            if (this.filterEnhancers != null && !this.filterEnhancers.isEmpty()) {
                this.filterEnhancers.forEach(p -> p.after(tuple.getF(), tuple.getS()));
            }
            afterFilter(tuple.getF(), tuple.getS());
        }
    }

    protected Class<?> loadClass(String className) throws ServletException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e1) {
                throw new ServletException(e1);
            }
        }
    }

}
