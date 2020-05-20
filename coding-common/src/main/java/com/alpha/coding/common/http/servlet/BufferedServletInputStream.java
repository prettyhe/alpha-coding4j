package com.alpha.coding.common.http.servlet;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.servlet.ServletInputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * BufferedServletInputStream
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class BufferedServletInputStream extends ServletInputStreamWrapper {

    private BufferedInputStream bufferedInputStream;

    public BufferedServletInputStream(ServletInputStream servletInputStream) {
        super(servletInputStream);
        this.bufferedInputStream = new BufferedInputStream(servletInputStream);
    }

    @Override
    public int read() throws IOException {
        return bufferedInputStream.read();
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return bufferedInputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return bufferedInputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return bufferedInputStream.available();
    }

    @Override
    public void close() throws IOException {
        bufferedInputStream.close();
    }

    @Override
    public void mark(int readLimit) {
        bufferedInputStream.mark(readLimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        bufferedInputStream.reset();
    }

    public boolean markSupported() {
        return bufferedInputStream.markSupported();
    }

}
