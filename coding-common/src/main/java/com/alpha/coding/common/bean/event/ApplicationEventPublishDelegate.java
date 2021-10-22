package com.alpha.coding.common.bean.event;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.alpha.coding.bo.executor.RunnableWrapper;

import lombok.Setter;

/**
 * ApplicationEventPublishDelegate
 *
 * @version 1.0
 * Date: 2021/6/29
 */
public class ApplicationEventPublishDelegate implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Setter
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationEventPublishDelegate.applicationContext = applicationContext;
    }

    private void doPublishEvent(ApplicationEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    public static void publishEvent(ApplicationEvent event) {
        applicationContext.getBean(ApplicationEventPublishDelegate.class).doPublishEvent(event);
    }

    /**
     * 发布回调事件：在事务中通过监听器执行；非事务中直接执行
     */
    public static void publishCallbackEvent(AsyncCallbackApplicationEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 在事务中
            publishEvent(event);
        } else {
            // 不在事务中
            final Runnable runnable = (Runnable) event.getSource();
            if (runnable instanceof RunnableWrapper) {
                ((RunnableWrapper) runnable).rawRun();
            } else {
                runnable.run();
            }
        }
    }

}
