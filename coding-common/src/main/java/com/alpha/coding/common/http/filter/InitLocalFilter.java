package com.alpha.coding.common.http.filter;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.constant.Keys;
import com.alpha.coding.bo.function.common.Predicates;
import com.alpha.coding.common.http.CookieUtils;
import com.alpha.coding.common.http.HttpParameterUtils;
import com.alpha.coding.common.utils.IpUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * InitLocalFilter
 *
 * @version 1.0
 * Date: 2019-12-27
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class InitLocalFilter extends AbstractEnhancerFilter {

    private Class<?> envContextClass;

    @Override
    public void postInit(FilterConfig filterConfig) throws ServletException {
        final String className = filterConfig.getInitParameter("envContextClass");
        if (Predicates.isNotBlankStr.test(className)) {
            this.envContextClass = loadClass(className);
        }
    }

    @Override
    public Tuple<ServletRequest, ServletResponse> beforeFilter(ServletRequest servletRequest,
                                                               ServletResponse servletResponse) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            MapThreadLocalAdaptor.put(Keys.CURRENT_URI,
                    request.getRequestURI().substring(request.getContextPath().length()));
            MapThreadLocalAdaptor.put(Keys.CLIENT_ID, parseClientId(request));
            MapThreadLocalAdaptor.put(Keys.CLIENT_IP, IpUtils.getClientIp(request));
            MapThreadLocalAdaptor.put(Keys.TOKEN, request.getHeader("Authorization"));
            MapThreadLocalAdaptor.put(Keys.ENV_CONTEXT, parseEnvContext(request));
        } catch (Exception e) {
            log.warn("Init MapThreadLocalAdaptor fail for {}", request.getRequestURI(), e);
        }
        if (log.isDebugEnabled()) {
            log.debug("CURRENT_ENV:: uri:{},clientId:{},clientIp:{},context:{}",
                    MapThreadLocalAdaptor.get(Keys.CURRENT_URI),
                    MapThreadLocalAdaptor.get(Keys.CLIENT_ID),
                    MapThreadLocalAdaptor.get(Keys.CLIENT_IP),
                    JSON.toJSONString(MapThreadLocalAdaptor.get(Keys.ENV_CONTEXT)));
        }
        return Tuple.of(request, response);
    }

    private String parseClientId(HttpServletRequest request) {
        String clientId = CookieUtils.getCookieValue(request, Keys.COOKIE_KEY_CLIENT_ID);
        if (clientId == null) {
            clientId = request.getHeader(Keys.HEADER_CLIENT_ID);
        }
        return clientId;
    }

    protected Object parseEnvContext(HttpServletRequest request) throws IOException {
        if (this.envContextClass == null) {
            return null;
        }
        Object envContext = null;
        try {
            envContext = this.envContextClass.newInstance();
            HttpParameterUtils.parseHttpParameter(request, envContext);
        } catch (Exception e) {
            log.error("ParseParamErr for {}", request.getRequestURI());
        }
        return envContext;
    }

    @Override
    public void afterFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
        MapThreadLocalAdaptor.remove(Keys.CURRENT_URI);
        MapThreadLocalAdaptor.remove(Keys.CLIENT_ID);
        MapThreadLocalAdaptor.remove(Keys.CLIENT_IP);
        MapThreadLocalAdaptor.remove(Keys.TOKEN);
        MapThreadLocalAdaptor.remove(Keys.ENV_CONTEXT);
    }

    @Override
    public void destroy() {

    }
}
