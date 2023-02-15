package com.alpha.coding.bo.executor;

import java.util.concurrent.Callable;

import com.alpha.coding.bo.function.ThrowableFunction;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * CallableWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CallableWrapper<V> implements Callable<V> {

    private Callable<V> command;
    private Runnable before;
    private Runnable after;
    private ThrowableFunction<Exception, V> whenException;

    public static <V> CallableWrapper<V> of() {
        return new CallableWrapper<>();
    }

    public static <V> CallableWrapper<V> of(Callable<V> command) {
        return new CallableWrapper<>(command);
    }

    public static <V> CallableWrapper<V> of(Runnable command) {
        return new CallableWrapper<>(command);
    }

    public static <V> CallableWrapper<V> of(Callable<V> command, Runnable before) {
        return new CallableWrapper<>(command, before);
    }

    public static <V> CallableWrapper<V> of(Callable<V> command, Runnable before, Runnable after) {
        return new CallableWrapper<>(command, before, after);
    }

    public static <V> CallableWrapper<V> of(Callable<V> command, Runnable before, Runnable after,
                                            ThrowableFunction<Exception, V> whenException) {
        return new CallableWrapper<>(command, before, after, whenException);
    }

    public CallableWrapper(final Runnable command) {
        this.command = command == null ? null : () -> {
            command.run();
            return null;
        };
    }

    public CallableWrapper(Callable<V> command) {
        this.command = command;
    }

    public CallableWrapper(Callable<V> command, Runnable before) {
        this.command = command;
        this.before = before;
    }

    public CallableWrapper(Callable<V> command, Runnable before, Runnable after) {
        this.command = command;
        this.before = before;
        this.after = after;
    }

    public CallableWrapper(Callable<V> command, Runnable before, Runnable after,
                           ThrowableFunction<Exception, V> whenException) {
        this.command = command;
        this.before = before;
        this.after = after;
        this.whenException = whenException;
    }

    @Override
    public V call() throws Exception {
        return dynamicCall(this.command);
    }

    public V dynamicCall(Callable<V> callable) throws Exception {
        if (whenException != null) {
            try {
                if (before != null) {
                    before.run();
                }
                return callable.call();
            } catch (Exception e) {
                try {
                    return whenException.apply(e);
                } catch (Throwable throwable) {
                    if (throwable instanceof Exception) {
                        throw (Exception) throwable;
                    } else {
                        throw new Exception(throwable);
                    }
                }
            } finally {
                if (after != null) {
                    after.run();
                }
            }
        } else {
            try {
                if (before != null) {
                    before.run();
                }
                return callable.call();
            } finally {
                if (after != null) {
                    after.run();
                }
            }
        }
    }

    /**
     * 原生执行
     */
    public V rawCall() throws Exception {
        return this.command.call();
    }

}
