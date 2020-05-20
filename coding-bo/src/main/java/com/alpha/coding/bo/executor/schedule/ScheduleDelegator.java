package com.alpha.coding.bo.executor.schedule;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.alpha.coding.bo.executor.RunnableWrapper;
import com.alpha.coding.bo.executor.SelfRefRunnable;

import lombok.extern.slf4j.Slf4j;

/**
 * ScheduleDelegator
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class ScheduleDelegator {

    private ScheduledExecutorService executorService;

    public ScheduleDelegator(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * 任务调度
     *
     * @param task 任务
     */
    public void schedule(final ScheduledTask task) {
        schedule(task, null);
    }

    /**
     * 任务调度
     *
     * @param task            任务
     * @param runnableWrapper 执行包装器
     */
    public void schedule(final ScheduledTask task, final RunnableWrapper runnableWrapper) {
        final AtomicLong runCount = task.getRunCount();
        final AtomicLong scheduleNanos = task.getLatestScheduleNanos();
        final RunnableWrapper wrapper = runnableWrapper == null ? RunnableWrapper.none() : runnableWrapper;
        final SelfRefRunnable selfRefRunnable = new SelfRefRunnable((SelfRefRunnable runnable) ->
                wrapper.dynamicRun(() -> {
                    final long nanos = System.nanoTime();
                    scheduleNanos.set(nanos);
                    ScheduledTask before = new ScheduledTask()
                            .setScheduledMode(task.getScheduledMode())
                            .setInitialDelay(task.getInitialDelay())
                            .setPeriod(task.getPeriod())
                            .setTimeUnit(task.getTimeUnit());
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("ScheduledTask-{}-run-{} start", task.getTaskId(), runCount.get() + 1);
                        }
                        task.run();
                    } finally {
                        runCount.incrementAndGet();
                        if (log.isDebugEnabled()) {
                            log.debug("ScheduledTask-{}-run-{} finished, elapse {}ms",
                                    task.getTaskId(), runCount.get(),
                                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanos));
                        }
                        switch (task.getScheduledMode()) {
                            case Once:
                                if (before.getScheduledMode() == ScheduledTask.ScheduledMode.Once) {
                                    break;
                                } else {
                                    executorService.schedule(runnable, task.getPeriod(), task.getTimeUnit());
                                }
                                break;
                            case FixedRate:
                                long delay = scheduleNanos.get() + task.getTimeUnit().toNanos(task.getPeriod())
                                        - System.nanoTime();
                                executorService.schedule(runnable, delay <= 0 ? 0 : delay, TimeUnit.NANOSECONDS);
                                break;
                            case FixedDelay:
                                executorService.schedule(runnable, task.getPeriod(), task.getTimeUnit());
                                break;
                            default:
                                log.warn("unknown ScheduledMode");
                                break;
                        }
                    }
                }));
        executorService.schedule(selfRefRunnable, task.getInitialDelay(), task.getTimeUnit());
    }

}
