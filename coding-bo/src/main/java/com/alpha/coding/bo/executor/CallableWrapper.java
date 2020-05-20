package com.alpha.coding.bo.executor;

import java.util.concurrent.Callable;

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

    @Override
    public V call() throws Exception {
        try {
            if (before != null) {
                before.run();
            }
            return command.call();
        } finally {
            if (after != null) {
                after.run();
            }
        }
    }
}
