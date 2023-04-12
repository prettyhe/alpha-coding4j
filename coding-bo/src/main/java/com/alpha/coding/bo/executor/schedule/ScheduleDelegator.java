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

    private final ScheduledExecutorService executorService;

    public ScheduleDelegator(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public static ScheduleDelegator build(ScheduledExecutorService executorService) {
        return new ScheduleDelegator(executorService);
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
        final SelfRefRunnable selfRefRunnable = new SelfRefRunnable((SelfRefRunnable runnable) -> {
            wrapper.dynamicRun(() -> {
                final long startTime = System.nanoTime();
                scheduleNanos.set(startTime);
                final long currentRunCount = runCount.incrementAndGet();
                final ScheduledTask.ScheduledMode currentScheduledMode = task.getScheduledMode();
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("ScheduledTask:{} run:{} start", task.getTaskId(), currentRunCount);
                    }
                    task.run();
                } catch (Throwable throwable) {
                    log.warn("ScheduledTask:{} run:{} Runnable execute fail",
                            task.getTaskId(), currentRunCount, throwable);
                    throw throwable;
                } finally {
                    if (log.isDebugEnabled()) {
                        final long endTime = System.nanoTime();
                        log.debug("ScheduledTask:{} run:{} finished, elapsed:{}ms, nextMode:{}",
                                task.getTaskId(), currentRunCount,
                                TimeUnit.NANOSECONDS.toMillis(endTime - startTime),
                                task.getScheduledMode());
                    }
                    switch (task.getScheduledMode()) {
                        case Once:
                            if (currentScheduledMode == ScheduledTask.ScheduledMode.Once) {
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
                            log.warn("unknown ScheduledMode:" + task.getScheduledMode());
                            break;
                    }
                }
            });
        });
        executorService.schedule(selfRefRunnable, task.getInitialDelay(), task.getTimeUnit());
    }

}
