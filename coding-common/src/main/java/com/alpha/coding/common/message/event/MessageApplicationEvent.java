package com.alpha.coding.common.message.event;

import org.springframework.context.ApplicationEvent;

import com.alpha.coding.common.message.dal.MessageMonitor;

/**
 * MessageApplicationEvent
 *
 * @version 1.0
 * Date: 2021/7/3
 */
public class MessageApplicationEvent extends ApplicationEvent {

    public MessageApplicationEvent(MessageMonitor source) {
        super(source);
    }

}
