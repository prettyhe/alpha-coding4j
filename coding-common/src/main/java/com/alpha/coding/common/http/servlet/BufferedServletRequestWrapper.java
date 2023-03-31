package com.alpha.coding.common.http.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alpha.coding.common.http.HttpParameterUtils;
import com.alpha.coding.common.http.HttpUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * BufferedServletRequestWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

    private final BufferedServletInputStream bufferedServletInputStream;
    private volatile MultiValueMap<String, String> cachedParameterMap;

    public BufferedServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.bufferedServletInputStream = new BufferedServletInputStream(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.bufferedServletInputStream;
    }

    @Override
    public String getParameter(String name) {
        if (this.isFormPost()) {
            this.parseCachedParameterMap();
            final List<String> values = this.cachedParameterMap.get(name);
            if (values != null) {
                return values.size() == 0 ? "" : values.get(0);
            } else {
                return null;
            }
        }
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (this.isFormPost()) {
            this.parseCachedParameterMap();
            final Map<String, String[]> map = new LinkedHashMap<>();
            for (String name : this.cachedParameterMap.keySet()) {
                final List<String> values = this.cachedParameterMap.get(name);
                map.put(name, values == null ? null : values.toArray(new String[0]));
            }
            return Collections.unmodifiableMap(map);
        }
        return super.getParameterMap();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (this.isFormPost()) {
            this.parseCachedParameterMap();
            return Collections.enumeration(this.cachedParameterMap.keySet());
        }
        return super.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        if (this.isFormPost()) {
            this.parseCachedParameterMap();
            final List<String> values = this.cachedParameterMap.get(name);
            return values == null ? null : values.toArray(new String[0]);
        }
        return super.getParameterValues(name);
    }

    /**
     * 是否是POST请求且是application/x-www-form-urlencoded
     */
    private boolean isFormPost() {
        return HttpParameterUtils.isFormContentType(this) && HttpMethod.POST.matches(getMethod());
    }

    /**
     * 解析ParameterMap
     */
    private void parseCachedParameterMap() {
        if (this.cachedParameterMap == null) {
            synchronized(this) {
                if (this.cachedParameterMap == null) {
                    try {
                        this.cachedParameterMap = HttpUtils.readBodyFormParams(this);
                    } catch (IOException e) {
                        log.warn("parseParameterMap fail", e);
                        this.cachedParameterMap = new LinkedMultiValueMap<>(0);
                    }
                }
            }
        }
    }

}
