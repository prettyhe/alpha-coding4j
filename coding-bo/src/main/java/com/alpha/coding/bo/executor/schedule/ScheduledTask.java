package com.alpha.coding.bo.executor.schedule;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * ScheduledTask
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class ScheduledTask implements Runnable {

    private static final AtomicLong TASK_CNT = new AtomicLong(0);

    private volatile Consumer<ScheduledTask> command;
    private volatile ScheduledMode scheduledMode = ScheduledMode.Once;
    private volatile long initialDelay;
    private volatile long period;
    private volatile TimeUnit timeUnit;

    private final String taskId = System.currentTimeMillis() + "_" + TASK_CNT.incrementAndGet();
    @Getter(value = AccessLevel.PACKAGE)
    final AtomicLong runCount = new AtomicLong(0);
    @Getter(value = AccessLevel.PACKAGE)
    final AtomicLong latestScheduleNanos = new AtomicLong(0);

    @Override
    public void run() {
        this.command.accept(this); // 包含自身的引用，方便执行时动态修改
    }

    /**
     * 调度模式
     */
    public static enum ScheduledMode {
        /**
         * 执行一次
         */
        Once,
        /**
         * 固定频率
         */
        FixedRate,
        /**
         * 固定延时
         */
        FixedDelay;
    }

    public long runCount() {
        return this.runCount.get();
    }

    public long latestScheduleNanos() {
        return this.latestScheduleNanos.get();
    }
}
