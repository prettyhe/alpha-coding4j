package com.alpha.coding.common.http.filter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.base.MapThreadLocalMirrorAspect;
import com.alpha.coding.bo.base.Tuple;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * MapThreadLocalMirrorFilter
 *
 * @version 1.0
 * Date: 2022/4/29
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class MapThreadLocalMirrorFilter extends AbstractEnhancerFilter {

    private MapThreadLocalMirrorAspect mirrorAspect = new MapThreadLocalMirrorAspect();
    private Map<String, String> keyTransformMap = new HashMap<>();

    @Override
    public void postInit(FilterConfig filterConfig) throws ServletException {
        final String keyTransformMapStr = filterConfig.getInitParameter("keyTransformMap");
        if (keyTransformMapStr != null) {
            for (String kv : keyTransformMapStr.split(",")) {
                final String[] ss = kv.split("=");
                if (ss.length < 2) {
                    continue;
                }
                keyTransformMap.put(ss[0].trim(), ss[1].trim());
            }
        }
    }

    @Override
    public Tuple<ServletRequest, ServletResponse> beforeFilter(ServletRequest servletRequest,
                                                               ServletResponse servletResponse) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        mirrorAspect.doBefore();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final String transformHeaderName = transformKey(headerName);
            final Enumeration<String> headerValues = request.getHeaders(headerName);
            boolean first = false;
            while (headerValues != null && headerValues.hasMoreElements()) {
                final String headerValue = headerValues.nextElement();
                if (headerValue == null) {
                    continue;
                }
                if (!first) {
                    MapThreadLocalAdaptor.put(transformHeaderName, headerValue);
                    first = true;
                }
                if (!headerValue.isEmpty()) {
                    MapThreadLocalAdaptor.put(transformHeaderName, headerValue);
                    break;
                }
            }
            if (!MapThreadLocalAdaptor.containsKey(transformHeaderName)) {
                MapThreadLocalAdaptor.put(transformHeaderName, null);
            }
        }
        return Tuple.of(request, response);
    }

    @Override
    public void afterFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
        mirrorAspect.doAfter();
    }

    @Override
    public void destroy() {

    }

    private String transformKey(String key) {
        return Optional.ofNullable(keyTransformMap.get(key)).orElse(key);
    }
}
