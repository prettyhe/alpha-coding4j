package com.alpha.coding.bo.executor;

import java.util.function.Consumer;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * RunnableWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class RunnableWrapper implements Runnable {

    private static final RunnableWrapper NONE = new RunnableWrapper(() -> {
        // do nothing
    });

    private Runnable command;
    private Runnable before;
    private Runnable after;
    private Consumer<Exception> whenException;

    public static RunnableWrapper none() {
        return NONE;
    }

    public static RunnableWrapper of() {
        return new RunnableWrapper();
    }

    public static RunnableWrapper of(Runnable command) {
        return new RunnableWrapper(command);
    }

    public static RunnableWrapper of(Runnable command, Runnable before) {
        return new RunnableWrapper(command, before);
    }

    public static RunnableWrapper of(Runnable command, Runnable before, Runnable after) {
        return new RunnableWrapper(command, before, after);
    }

    public static RunnableWrapper of(Runnable command, Runnable before, Runnable after,
                                     Consumer<Exception> whenException) {
        return new RunnableWrapper(command, before, after, whenException);
    }

    public RunnableWrapper() {
    }

    public RunnableWrapper(Runnable command) {
        this.command = command;
    }

    public RunnableWrapper(Runnable command, Runnable before) {
        this.command = command;
        this.before = before;
    }

    public RunnableWrapper(Runnable command, Runnable before, Runnable after) {
        this.command = command;
        this.before = before;
        this.after = after;
    }

    public RunnableWrapper(Runnable command, Runnable before, Runnable after,
                           Consumer<Exception> whenException) {
        this.command = command;
        this.before = before;
        this.after = after;
        this.whenException = whenException;
    }

    @Override
    public void run() {
        dynamicRun(this.command);
    }

    /**
     * 动态执行
     *
     * @param task 调用时执行指定Runnable
     */
    public void dynamicRun(Runnable task) {
        if (whenException != null) {
            try {
                if (this.before != null) {
                    this.before.run();
                }
                task.run();
            } catch (Exception e) {
                whenException.accept(e);
            } finally {
                if (this.after != null) {
                    this.after.run();
                }
            }
        } else {
            try {
                if (this.before != null) {
                    this.before.run();
                }
                task.run();
            } finally {
                if (this.after != null) {
                    this.after.run();
                }
            }
        }
    }

    /**
     * 原生执行
     */
    public void rawRun() {
        this.command.run();
    }

}
