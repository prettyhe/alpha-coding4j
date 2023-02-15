package com.alpha.coding.bo.executor;

import java.util.concurrent.Callable;

import com.alpha.coding.bo.function.ThrowableFunction;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * CallableToRunnableWrapper
 *
 * @version 1.0
 * Date: 2023/2/15
 */
@Slf4j
public class CallableToRunnableWrapper<V> implements Runnable {

    private final Callable<V> callable;
    private ThrowableFunction<Exception, V> whenException;
    @Getter
    private V result;
    @Getter
    private Exception exception;
    @Getter
    private V rawResult;
    @Getter
    private Exception rawException;

    public CallableToRunnableWrapper(Callable<V> callable) {
        this.callable = callable;
    }

    public CallableToRunnableWrapper(Callable<V> callable, ThrowableFunction<Exception, V> whenException) {
        this.callable = callable;
        this.whenException = whenException;
    }

    public static <V> CallableToRunnableWrapper<V> wrap(Callable<V> callable) {
        return new CallableToRunnableWrapper<>(callable);
    }

    public static <V> CallableToRunnableWrapper<V> wrap(Callable<V> callable,
                                                        ThrowableFunction<Exception, V> whenException) {
        return new CallableToRunnableWrapper<>(callable, whenException);
    }

    @Override
    public void run() {
        try {
            rawResult = callable.call();
            result = rawResult;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Exception occur when do call", e);
            }
            rawException = e;
            exception = e;
            if (whenException != null) {
                try {
                    result = whenException.apply(e);
                    exception = null;
                } catch (Throwable throwable) {
                    if (log.isDebugEnabled()) {
                        log.warn("Exception occur when do exception callback", e);
                    }
                    if (throwable instanceof Exception) {
                        exception = (Exception) throwable;
                    } else {
                        exception = new Exception(throwable);
                    }
                }
            }
        }
    }

    public boolean hasException() {
        return exception != null;
    }

}
