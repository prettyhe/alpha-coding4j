package com.alpha.coding.common.bean.async;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.executor.NamedExecutorPool;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * AsyncExeBean
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class AsyncExeBean implements InitializingBean, DisposableBean {

    private static final String POOL_NAME = "async";

    @Getter
    @Setter
    @Accessors(chain = true)
    private AsyncConfiguration asyncConfiguration = new AsyncConfiguration();

    @Setter
    @Accessors(chain = true)
    private Executor executor;

    @Setter
    @Accessors(chain = true)
    private Consumer<List> callback;

    @Setter
    @Accessors(chain = true)
    private Runnable monitorCallback;

    private volatile boolean initialized;
    private Executor monitor;
    private BlockingQueue queue;
    @Getter
    private volatile boolean running;
    private final AtomicInteger runningCnt = new AtomicInteger();

    public AsyncExeBean() {
        if (!initialized) {
            init();
        }
    }

    public AsyncExeBean(AsyncConfiguration asyncConfiguration) {
        this.asyncConfiguration = asyncConfiguration;
        if (!initialized) {
            init();
        }
    }

    private synchronized void init() {
        if (log.isDebugEnabled()) {
            log.debug("asyncConfiguration is {}", JSON.toJSONString(asyncConfiguration));
        }
        if (asyncConfiguration.getQueue() != null) {
            queue = asyncConfiguration.getQueue();
        } else {
            if (queue == null) {
                queue = new LinkedBlockingQueue(asyncConfiguration.getQueueCapacity());
            }
        }
        if (executor == null) {
            executor = NamedExecutorPool.newFixedThreadPool(POOL_NAME, asyncConfiguration.getCoreExeSize());
        }
        if (monitor == null) {
            monitor = NamedExecutorPool.newFixedThreadPool(POOL_NAME + "-monitor", 1);
        }
        initialized = true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public void destroy() throws Exception {
        stop();
        shutdown();
    }

    public void shutdown() {
        if (executor != null && executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdown();
        }
        if (monitor != null && monitor instanceof ExecutorService) {
            ((ExecutorService) monitor).shutdown();
        }
    }

    public void put(Object obj) throws InterruptedException, AsyncException {
        if (running) {
            queue.put(obj);
        } else {
            throw new AsyncException("cannot put to not yet started");
        }
    }

    public boolean add(Object obj) throws AsyncException {
        if (running) {
            return queue.add(obj);
        } else {
            throw new AsyncException("cannot add to not yet started");
        }
    }

    public boolean offer(Object obj) throws AsyncException {
        if (running) {
            return queue.offer(obj);
        } else {
            throw new AsyncException("cannot offer to not yet started");
        }
    }

    public synchronized void reStart() {
        stop();
        init();
        start();
    }

    public synchronized void start() {
        if (!initialized) {
            init();
        }
        if (running) {
            log.info("{} already start", asyncConfiguration.getIdentify());
            return;
        }
        running = true;
        startMonitor();
        for (int i = 0; i < asyncConfiguration.getCoreExeSize(); i++) {
            executor.execute(() -> {
                while (running) {
                    List list = Lists.newArrayList();
                    queue.drainTo(list, asyncConfiguration.getBatchSize());
                    if (list.isEmpty()) {
                        sleep(asyncConfiguration.getSleepMillis());
                        continue;
                    }
                    runningCnt.incrementAndGet();
                    try {
                        callback.accept(list);
                    } finally {
                        runningCnt.decrementAndGet();
                    }
                }
            });
        }
    }

    public synchronized void stop() {
        running = false;
        while (queue.size() > 0) {
            List list = Lists.newArrayList();
            queue.drainTo(list, asyncConfiguration.getBatchSize());
            callback.accept(list);
        }
        while (!done()) {
            sleep(100);
        }
    }

    public boolean done() {
        return !running && isIdle();
    }

    public boolean isIdle() {
        return queue.size() == 0 && runningCnt.get() == 0;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startMonitor() {
        monitor.execute(() -> {
            int cnt = 0;
            while (true) {
                cnt++;
                int queueSize = queue.size();
                int runCnt = runningCnt.get();
                if (queueSize > 0 || runCnt > 0 || cnt >= asyncConfiguration.getMonitorMaxNopTimes()) {
                    log.info("{} state: running={}, queueSize={}, runningCnt={}",
                            asyncConfiguration.getIdentify(), running, queueSize, runCnt);
                    cnt = 0;
                    if (monitorCallback != null) {
                        monitorCallback.run();
                    }
                }
                sleep(asyncConfiguration.getMonitorNopSleepMillis());
            }
        });
    }

}
