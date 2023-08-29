package com.alpha.coding.common.bean.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.alpha.coding.bo.executor.NamedExecutorPool;
import com.alpha.coding.bo.executor.RunnableWrapper;
import com.alpha.coding.bo.executor.schedule.ScheduleDelegator;
import com.alpha.coding.bo.executor.schedule.ScheduledTask;

import lombok.extern.slf4j.Slf4j;

/**
 * ContextWarmUpInitializer
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class ContextWarmUpInitializer implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    private final AtomicInteger initFlag = new AtomicInteger(0);
    private final List<ExecutorService> executorServiceList = new ArrayList<>();
    private Function<Runnable, RunnableWrapper> warmUpAopProvider = null; // 切面逻辑控制

    public ContextWarmUpInitializer() {
    }

    public ContextWarmUpInitializer(Function<Runnable, RunnableWrapper> warmUpAopProvider) {
        this.warmUpAopProvider = warmUpAopProvider;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        log.info("listened ContextRefreshedEvent for ctx={}, parent={}",
                getApplicationContextName(context),
                getApplicationContextName(context.getParent()));
        synchronized(initFlag) {
            if (initFlag.get() > 0) {
                log.info("already warm up......");
                return;
            }
            log.info("warm up......");
            try {
                doWarmUp(context);
                doAsyncWarmUp(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            initFlag.incrementAndGet();
        }
    }

    private String getApplicationContextName(ApplicationContext ctx) {
        return ctx == null ? null : ctx.getApplicationName();
    }

    /**
     * 同步暖启动
     */
    private void doWarmUp(ApplicationContext applicationContext) throws Exception {
        Map<String, WarmUpCallback> beans = applicationContext.getBeansOfType(WarmUpCallback.class);
        for (final Map.Entry<String, WarmUpCallback> entry : beans.entrySet()) {
            log.info("do warm up for bean={}", entry.getKey());
            if (warmUpAopProvider == null) {
                entry.getValue().doWarmUp();
            } else {
                final Exception[] exceptions = new Exception[1];
                final Runnable task = () -> {
                    try {
                        entry.getValue().doWarmUp();
                    } catch (Exception e) {
                        exceptions[0] = e;
                    }
                };
                warmUpAopProvider.apply(task).dynamicRun(task); // 使用动态执行
                if (exceptions[0] != null) {
                    throw exceptions[0];
                }
            }
        }
    }

    /**
     * 异步暖启动
     */
    private void doAsyncWarmUp(ApplicationContext applicationContext) throws Exception {
        Map<String, AsyncWarmUpCallback> beans = applicationContext.getBeansOfType(AsyncWarmUpCallback.class);
        for (Map.Entry<String, AsyncWarmUpCallback> entry : beans.entrySet()) {
            log.info("do async warm up for bean={}", entry.getKey());
            final List<Runnable> tasks = entry.getValue().asyncWarmUp();
            if (tasks == null || tasks.size() == 0) {
                continue;
            }
            List<Runnable> commonTasks = new ArrayList<>(tasks.size());
            List<ScheduledTask> scheduledTasks = new ArrayList<>(tasks.size());
            for (Runnable task : tasks) {
                if (task instanceof ScheduledTask) {
                    scheduledTasks.add((ScheduledTask) task);
                } else {
                    commonTasks.add(task);
                }
            }
            if (commonTasks.size() > 0) {
                final ExecutorService executor = NamedExecutorPool.newFixedThreadPool("WarmUpPool", commonTasks.size());
                executorServiceList.add(executor);
                for (final Runnable task : commonTasks) {
                    if (warmUpAopProvider == null) {
                        executor.submit(task);
                    } else {
                        executor.submit(() -> warmUpAopProvider.apply(task).dynamicRun(task));
                    }
                }
            }
            if (scheduledTasks.size() > 0) {
                final ScheduledExecutorService executor =
                        NamedExecutorPool.newScheduledThreadPool("WarmUpSchPool", scheduledTasks.size());
                executorServiceList.add(executor);
                ScheduleDelegator delegator = new ScheduleDelegator(executor);
                for (final ScheduledTask task : scheduledTasks) {
                    delegator.schedule(task, warmUpAopProvider == null ? null : warmUpAopProvider.apply(task));
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        for (ExecutorService executorService : executorServiceList) {
            try {
                executorService.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
