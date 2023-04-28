package com.alpha.coding.common.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.executor.CallableWrapper;
import com.alpha.coding.bo.executor.NamedThreadFactory;
import com.alpha.coding.bo.executor.RunnableWrapper;
import com.alpha.coding.bo.function.SelfChainConsumer;

import lombok.Getter;
import lombok.Setter;

/**
 * MDCThreadPoolTaskExecutor
 * <p>
 * 重写execute、submit等方法，拷贝上级线程的MDC变量、MapThreadLocalAdaptor变量到子线程
 * </p>
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MDCThreadPoolTaskExecutor extends ThreadPoolTaskExecutor
        implements ExecutorService, SelfChainConsumer<MDCThreadPoolTaskExecutor> {

    private final AtomicBoolean initializeFlag = new AtomicBoolean(false);

    @Setter
    @Getter
    private Consumer<Map<String, String>> mdcContextConsumer;

    @Getter
    @Setter
    private Runnable beforeRun;

    @Getter
    @Setter
    private Runnable afterRun;

    @Getter
    @Setter
    private Consumer<Exception> whenRunException;

    /**
     * 需要手动执行initialize()
     */
    public MDCThreadPoolTaskExecutor() {
        super();
    }

    /**
     * get builder
     */
    public static MDCThreadPoolTaskExecutorBuilder builder() {
        return new MDCThreadPoolTaskExecutorBuilder();
    }

    /**
     * builder
     */
    public static class MDCThreadPoolTaskExecutorBuilder {

        private final MDCThreadPoolTaskExecutor executor = new MDCThreadPoolTaskExecutor();

        private MDCThreadPoolTaskExecutorBuilder() {
        }

        public MDCThreadPoolTaskExecutorBuilder corePoolSize(int corePoolSize) {
            this.executor.setCorePoolSize(corePoolSize);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder maxPoolSize(int maxPoolSize) {
            this.executor.setMaxPoolSize(maxPoolSize);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder queueCapacity(int queueCapacity) {
            this.executor.setQueueCapacity(queueCapacity);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder keepAliveSeconds(int keepAliveSeconds) {
            this.executor.setKeepAliveSeconds(keepAliveSeconds);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder threadFactory(ThreadFactory threadFactory) {
            this.executor.setThreadFactory(threadFactory);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder namedThreadFactory(String name) {
            this.executor.setThreadFactory(new NamedThreadFactory(name));
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder rejectedExecutionHandler(
                RejectedExecutionHandler rejectedExecutionHandler) {
            this.executor.setRejectedExecutionHandler(rejectedExecutionHandler);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
            this.executor.setAllowCoreThreadTimeOut(allowCoreThreadTimeOut);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder awaitTerminationSeconds(int awaitTerminationSeconds) {
            this.executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder mdcContextConsumer(Consumer<Map<String, String>> consumer) {
            this.executor.setMdcContextConsumer(consumer);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder daemon(boolean daemon) {
            this.executor.setDaemon(daemon);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder threadNamePrefix(String threadNamePrefix) {
            this.executor.setThreadNamePrefix(threadNamePrefix);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder threadPriority(int threadPriority) {
            this.executor.setThreadPriority(threadPriority);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder threadGroup(ThreadGroup threadGroup) {
            this.executor.setThreadGroup(threadGroup);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder threadGroupName(String threadGroupName) {
            this.executor.setThreadGroupName(threadGroupName);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder beanName(String beanName) {
            this.executor.setBeanName(beanName);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder beforeRun(Runnable beforeRun) {
            this.executor.setBeforeRun(beforeRun);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder afterRun(Runnable afterRun) {
            this.executor.setAfterRun(afterRun);
            return this;
        }

        public MDCThreadPoolTaskExecutorBuilder whenRunException(Consumer<Exception> whenRunException) {
            this.executor.setWhenRunException(whenRunException);
            return this;
        }

        public MDCThreadPoolTaskExecutor build() {
            if (!this.executor.initializeFlag.get()) {
                this.executor.initialize();
            }
            return this.executor;
        }

    }

    @Override
    public void initialize() {
        super.initialize();
        initializeFlag.set(true);
    }

    @Override
    public void afterPropertiesSet() {
        if (!initializeFlag.get()) {
            super.afterPropertiesSet();
        }
    }

    @Override
    public void execute(Runnable task) {
        super.execute(new MDCRunnableWrapper(RunnableWrapper.of(task, beforeRun, afterRun, whenRunException),
                doMDCContextConsume(), MapThreadLocalAdaptor.getCopyOfContextMap()));
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        super.execute(new MDCRunnableWrapper(RunnableWrapper.of(task, beforeRun, afterRun, whenRunException),
                doMDCContextConsume(), MapThreadLocalAdaptor.getCopyOfContextMap()), startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(new MDCRunnableWrapper(RunnableWrapper.of(task, beforeRun, afterRun, whenRunException),
                doMDCContextConsume(), MapThreadLocalAdaptor.getCopyOfContextMap()));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return tasks == null ? null : getThreadPoolExecutor().invokeAll(transform(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return tasks == null ? null : getThreadPoolExecutor().invokeAll(transform(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return tasks == null ? null : getThreadPoolExecutor().invokeAny(transform(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return tasks == null ? null : getThreadPoolExecutor().invokeAny(transform(tasks), timeout, unit);
    }

    @Override
    public List<Runnable> shutdownNow() {
        return getThreadPoolExecutor().shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return getThreadPoolExecutor().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return getThreadPoolExecutor().isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return getThreadPoolExecutor().awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(new MDCCallableWrapper<>(CallableWrapper.of(task, beforeRun, afterRun),
                doMDCContextConsume(), MapThreadLocalAdaptor.getCopyOfContextMap()));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return super.getThreadPoolExecutor()
                .submit(new MDCRunnableWrapper(RunnableWrapper.of(task, beforeRun, afterRun, whenRunException),
                        doMDCContextConsume(), MapThreadLocalAdaptor.getCopyOfContextMap()), result);
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        return super.submitListenable(
                new MDCRunnableWrapper(RunnableWrapper.of(task, beforeRun, afterRun, whenRunException),
                        doMDCContextConsume(), MapThreadLocalAdaptor.getCopyOfContextMap()));
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        return super.submitListenable(new MDCCallableWrapper<>(CallableWrapper.of(task, beforeRun, afterRun),
                doMDCContextConsume(), MapThreadLocalAdaptor.getCopyOfContextMap()));
    }

    private Map<String, String> doMDCContextConsume() {
        final Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        if (mdcContextConsumer != null) {
            mdcContextConsumer.accept(copyOfContextMap);
        }
        return copyOfContextMap;
    }

    private <T> List<MDCCallableWrapper<T>> transform(Collection<? extends Callable<T>> tasks) {
        if (tasks == null) {
            return null;
        }
        List<MDCCallableWrapper<T>> callableWrappers = new ArrayList<>(tasks.size());
        final Map<String, String> superMDCContext = doMDCContextConsume();
        for (Callable<T> task : tasks) {
            callableWrappers.add(new MDCCallableWrapper<>(CallableWrapper.of(task, beforeRun, afterRun),
                    superMDCContext, MapThreadLocalAdaptor.getCopyOfContextMap()));
        }
        return callableWrappers;
    }

}
