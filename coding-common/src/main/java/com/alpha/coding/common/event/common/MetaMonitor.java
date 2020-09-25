package com.alpha.coding.common.event.common;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alpha.coding.bo.executor.NamedExecutorPool;
import com.alpha.coding.common.event.listener.MetaChangeListener;
import com.google.common.eventbus.EventBus;

import lombok.extern.slf4j.Slf4j;

/**
 * MetaMonitor
 *
 * @version 1.0
 * Date: 2020-02-20
 */
@Slf4j
@Component("eventMetaMonitor")
public class MetaMonitor implements InitializingBean, DisposableBean, ApplicationContextAware {

    private final EventBus eventBus = new EventBus("MetaMonitor");
    private static ApplicationContext applicationContext;
    private static BlockingQueue queue = new LinkedBlockingQueue();
    private ExecutorService executorService = NamedExecutorPool.newFixedThreadPool("MetaMonitor", 1);

    @Autowired
    private MetaChangeListener metaChangeListener;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MetaMonitor.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Optional.ofNullable(metaChangeListener).ifPresent(this.eventBus::register);
        executorService.submit(() -> {
            while (true) {
                try {
                    final Object event = queue.take();
                    eventBus.post(event);
                } catch (Exception e) {
                    log.error("post event fail, msg:{}", e.getMessage());
                }
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        Optional.ofNullable(metaChangeListener).ifPresent(this.eventBus::unregister);
    }

    public static void post(Object event) {
        if (event == null) {
            return;
        }
        if (applicationContext == null) {
            log.error("ApplicationContext not set");
            queue.offer(event);
            return;
        }
        applicationContext.getBeansOfType(MetaMonitor.class).forEach((k, v) -> v.eventBus.post(event));
    }

}
