package com.alpha.coding.common.bean.async;

/**
 * AsyncException
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class AsyncException extends RuntimeException {

    public AsyncException(String msg) {
        super(msg);
    }

    public AsyncException(Throwable cause) {
        super(cause);
    }

    public AsyncException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
