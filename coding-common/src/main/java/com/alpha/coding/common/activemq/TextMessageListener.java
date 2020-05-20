package com.alpha.coding.common.activemq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.listener.SessionAwareMessageListener;

import com.alpha.coding.common.bean.comm.SelfRefBean;
import com.alpha.coding.common.log.LogMonitorIgnore;

import lombok.extern.slf4j.Slf4j;

/**
 * TextMessageListener
 *
 * @version 1.0
 * Date: 2020-04-04
 */
@Slf4j
public abstract class TextMessageListener implements SessionAwareMessageListener, SelfRefBean<TextMessageListener> {

    @Override
    public void onMessage(@LogMonitorIgnore Message message, @LogMonitorIgnore Session session) throws JMSException {
        try {
            final String text = ((TextMessage) message).getText();
            if (log.isDebugEnabled()) {
                log.debug("Received-TextMessage: id={},text={},message={}", message.getJMSMessageID(), text, message);
            }
            if (self() != null) {
                self().handle(text);
            } else {
                handle(text);
            }
        } catch (Exception e) {
            log.error("consume message fail, message={}", message, e);
            throw e;
        }
    }

    /**
     * 消息处理
     */
    protected abstract void handle(String text);

}
