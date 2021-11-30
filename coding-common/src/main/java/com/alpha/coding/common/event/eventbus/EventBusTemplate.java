package com.alpha.coding.common.event.eventbus;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.bo.executor.NamedExecutorPool;
import com.alpha.coding.common.event.AbstractEvent;
import com.alpha.coding.common.event.common.EventBusChangeEvent;
import com.alpha.coding.common.event.common.MetaMonitor;
import com.alpha.coding.common.event.listener.EventListener;
import com.alpha.coding.common.event.listener.EventListenerFactory;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * EventBusTemplate
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class EventBusTemplate implements InitializingBean, DisposableBean, BeanFactoryPostProcessor, EventBus {

    @Setter
    private String eventIdentityClassName;

    @Setter
    private Class<? extends EnumWithCodeSupplier> identity;

    @Setter
    @Autowired
    protected EventListenerFactory eventListenerFactory;

    /**
     * guava EventBus 实例
     */
    @Setter
    protected AsyncEventBus eventBusInstance;

    protected BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    protected ExecutorService postExecutor = NamedExecutorPool.newFixedThreadPool("EventBusPost", 2);
    protected ExecutorService monitorExecutor = NamedExecutorPool.newFixedThreadPool("EventBusMonitor", 1);
    private static final int BATCH_SIZE = 100;
    protected boolean isRunning = false;
    protected AtomicLong postCnt = new AtomicLong();
    private BlockingQueue<Byte> notifyQueue = new LinkedBlockingQueue<>(5);

    @Override
    public void afterPropertiesSet() throws Exception {
        if (identity == null) {
            identity = (Class<? extends EnumWithCodeSupplier>) Class.forName(eventIdentityClassName, true,
                    Thread.currentThread().getContextClassLoader());
        }
        Collection<EventListener> eventListeners = eventListenerFactory.getEventListeners(getIdentity());
        if (eventListeners != null && !eventListeners.isEmpty()) {
            for (EventListener eventListener : eventListeners) {
                eventBusInstance.register(eventListener);
                if (log.isDebugEnabled()) {
                    log.debug("register {} for {}", eventListener, eventBusInstance);
                }
            }
        }
        isRunning = true;
        asyncPost();
        monitor();
        if (log.isDebugEnabled()) {
            log.debug("init EventBusTemplate for {}", this.identity.getName());
        }
    }

    @Override
    public void destroy() throws Exception {
        isRunning = false;
        while (queue.size() > 0) {
            sleep(200);
        }
        Collection<EventListener> eventListeners = eventListenerFactory.getEventListeners(getIdentity());
        if (eventListeners != null && !eventListeners.isEmpty()) {
            for (EventListener eventListener : eventListeners) {
                eventBusInstance.unregister(eventListener);
            }
        }
        postExecutor.shutdown();
        monitorExecutor.shutdown();
    }

    @Override
    public Class<? extends EnumWithCodeSupplier> getIdentity() {
        return this.identity;
    }

    /**
     * 发送事件，子类可覆盖，实现不同的策略，如缓存、延迟发送等
     */
    @Override
    public <K, E extends EnumWithCodeSupplier, AE extends AbstractEvent<K, E>> void post(AE event) {
        if (event == null) {
            return;
        }
        if (isRunning) {
            EventContext<AE> eventContext = new EventContext<>();
            eventContext.setEvent(event)
                    .setSuperMDCContext(MDC.getCopyOfContextMap())
                    .setSuperMapThreadLocalAdaptorContext(MapThreadLocalAdaptor.getCopyOfContextMap());
            queue.offer(eventContext);
            postCnt.incrementAndGet();
            notifyQueue.offer((byte) 0);
        }
    }

    /**
     * 异步发送，子类可覆盖
     */
    protected <K, E extends EnumWithCodeSupplier, AE extends AbstractEvent<K, E>> void asyncPost() {
        /**
         * 轮询任务
         */
        postExecutor.submit(() -> {
            if (log.isDebugEnabled()) {
                log.debug("start polling-event-post task");
            }
            while (true) {
                List<Object> events = Lists.newArrayList();
                queue.drainTo(events, BATCH_SIZE);
                if (events == null || events.isEmpty()) {
                    sleep(100);
                    continue;
                }
                handleEvents(events);
            }
        });
        /**
         * 通知任务
         */
        postExecutor.submit(() -> {
            if (log.isDebugEnabled()) {
                log.debug("start notify-event-post task");
            }
            while (true) {
                try {
                    notifyQueue.take();
                    List<Object> events = Lists.newArrayList();
                    queue.drainTo(events, BATCH_SIZE);
                    handleEvents(events);
                } catch (Exception e) {
                    log.warn("handle by notifyQueue error, msg={}", e.getMessage());
                }
            }
        });

    }

    private <K, E extends EnumWithCodeSupplier, AE extends AbstractEvent<K, E>> void handleEvents(List<Object> events) {
        if (events == null) {
            return;
        }
        events.forEach(p -> {
            EventContext<AE> eventContext = (EventContext<AE>) p;
            AE event = eventContext.getEvent();
            if (eventContext.getSuperMDCContext() != null && MDC.getMDCAdapter() != null) {
                eventContext.getSuperMDCContext().forEach((k, v) -> MDC.put(k, v));
            }
            if (eventContext.getSuperMapThreadLocalAdaptorContext() != null) {
                eventContext.getSuperMapThreadLocalAdaptorContext().forEach((k, v) -> MapThreadLocalAdaptor.put(k, v));
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("event-post: eventID={}", event.getEventID());
                }
                eventBusInstance.post(event);
            } finally {
                if (eventContext.getSuperMDCContext() != null && MDC.getMDCAdapter() != null) {
                    eventContext.getSuperMDCContext().keySet().forEach(k -> MDC.remove(k));
                }
                if (eventContext.getSuperMapThreadLocalAdaptorContext() != null) {
                    eventContext.getSuperMapThreadLocalAdaptorContext().keySet()
                            .forEach(k -> MapThreadLocalAdaptor.remove(k));
                }
            }
        });
    }

    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // nothing to do
        }
    }

    protected void monitor() {
        monitorExecutor.submit(() -> {
            int cnt = 0;
            while (true) {
                cnt++;
                int size = queue.size();
                if (size > 0 || cnt >= 30) {
                    log.info("{} ==> event-queue-size: {}, postTotal: {}", getIdentity(), size, postCnt.get());
                    cnt = 0;
                }
                sleep(20000);
            }
        });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        MetaMonitor.post(new EventBusChangeEvent().setEventBusName(this.identity.getName()));
    }

    @Data
    @Accessors(chain = true)
    protected static class EventContext<E> {

        private E event;
        private Map<String, String> superMDCContext;
        private Map<String, Object> superMapThreadLocalAdaptorContext;

    }

}
