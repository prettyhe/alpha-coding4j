package com.alpha.coding.common.http.servlet;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * BufferedServletRequestWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

    private final BufferedServletInputStream bufferedServletInputStream;

    public BufferedServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.bufferedServletInputStream = new BufferedServletInputStream(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return bufferedServletInputStream;
    }

}
