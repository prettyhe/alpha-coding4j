package com.alpha.coding.common.log;

/**
 * ProceedThrowable
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ProceedThrowable extends Throwable {

    private static final long serialVersionUID = -4298849641340995963L;

    public ProceedThrowable() {
        super();
    }

    public ProceedThrowable(Throwable cause) {
        super(cause);
    }

    public ProceedThrowable(String msg, Throwable cause) {
        super(msg, cause);
    }

}
