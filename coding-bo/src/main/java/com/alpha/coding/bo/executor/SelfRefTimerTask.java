package com.alpha.coding.bo.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.MDC;

/**
 * SelfRefTimerTask
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class SelfRefTimerTask extends TimerTask {

    private static final AtomicLong COUNT = new AtomicLong(0L);

    private final long seq;
    private Consumer<TimerTask> consumeCommand;
    private Map<String, String> superMDCContext;

    public SelfRefTimerTask(Consumer<TimerTask> consumeCommand) {
        this.seq = COUNT.incrementAndGet();
        this.consumeCommand = consumeCommand;
        safeRun(() -> superMDCContext = new HashMap<>(MDC.getCopyOfContextMap()));
    }

    public long getSeq() {
        return this.seq;
    }

    @Override
    public void run() {
        try {
            safeRun(() -> superMDCContext.forEach((k, v) -> MDC.put(k, v)));
            if (consumeCommand != null) {
                consumeCommand.accept(this);
            }
        } finally {
            safeRun(() -> superMDCContext.keySet().forEach(k -> MDC.remove(k)));
        }
    }

    private void safeRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            //
        }
    }
}
