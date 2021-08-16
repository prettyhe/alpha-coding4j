package com.alpha.coding.bo.executor;

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

    private static final RunnableWrapper NONE = new RunnableWrapper(() -> {});

    private Runnable command;
    private Runnable before;
    private Runnable after;

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

    public static RunnableWrapper none() {
        return NONE;
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

    /**
     * 原生执行
     */
    public void rawRun() {
        this.command.run();
    }
}
