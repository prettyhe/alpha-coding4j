package com.alpha.coding.common.bean.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;

/**
 * ApplicationEventListener
 *
 * @version 1.0
 * Date: 2021/6/29
 */
@Slf4j
public class ApplicationEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, value = AsyncCallbackApplicationEvent.class)
    public void asyncCallbackApplicationEventHandle(AsyncCallbackApplicationEvent applicationEvent) {
        try {
            log.info("收到事务提交监控事件,异步处理 => {}", applicationEvent.getEventKey());
            final Runnable runnable = (Runnable) applicationEvent.getSource();
            if (runnable != null) {
                runnable.run();
            }
        } catch (Exception e) {
            log.error("事务提交监控事件后置处理异常,异步处理  => {}", applicationEvent.getEventKey(), e);
        }
    }

}
