package com.alpha.coding.common.executor;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alpha.coding.bo.executor.SelfRefTimerTask;
import com.google.common.collect.Lists;

/**
 * TimerExecutor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class TimerExecutor implements InitializingBean, DisposableBean {

    private final Timer scheduleTimer = new Timer("ScheduleTimer", true);
    private final Timer clearTimer = new Timer("ClearTimer", true);
    private final AtomicLong taskCnt = new AtomicLong(0);
    private Map<Long, TimerTask> cancelTaskMap = new ConcurrentHashMap<>();
    private long clearInterval = 60000; // 清理取消任务的时间间隔

    public void submitToSchedule(Consumer<Timer> timerConsumer) {
        if (timerConsumer != null) {
            timerConsumer.accept(scheduleTimer);
        }
    }

    public void submitToCancel(TimerTask toCancelTask) {
        submitToCancel(toCancelTask, 0);
    }

    public void submitToCancel(TimerTask toCancelTask, long delay) {
        if (toCancelTask != null) {
            scheduleTimer.schedule(new SelfRefTimerTask(t -> {
                try {
                    toCancelTask.cancel();
                    cancelTaskMap.put(taskCnt.getAndIncrement(), t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }), delay);
        }
    }

    public void start() {
        clearTimer.scheduleAtFixedRate(new SelfRefTimerTask(t -> {
            List<Long> taskIds = Lists.newArrayList(cancelTaskMap.keySet());
            for (Long taskId : taskIds) {
                try {
                    cancelTaskMap.get(taskId).cancel();
                } catch (Exception e) {
                    //
                }
                cancelTaskMap.remove(taskId);
            }
            scheduleTimer.purge();
            clearTimer.purge();
        }), 100, clearInterval);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void shutdown() {
        scheduleTimer.cancel();
        clearTimer.cancel();
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }
}
