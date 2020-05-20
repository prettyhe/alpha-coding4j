/**
 * Copyright
 */
package com.alpha.coding.common.http;

import java.io.IOException;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

/**
 * BwopHttpRequestRetryHandler
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class BwopHttpRequestRetryHandler implements HttpRequestRetryHandler {

    /** the number of times a method will be retried */
    private final int retryCount;

    /**
     * Default constructor
     */
    public BwopHttpRequestRetryHandler(int retryCount) {
        super();
        this.retryCount = retryCount;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        return this.retryCount > executionCount;
    }

    /**
     * @return the maximum number of times a method will be retried
     */
    public int getRetryCount() {
        return this.retryCount;
    }

}
