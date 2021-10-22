package com.alpha.coding.common.rocketmq;

import java.util.List;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * MessageListenerConcurrentlyAdaptor
 *
 * @version 1.0
 * Date: 2021/8/17
 */
public class MessageListenerConcurrentlyAdaptor implements MessageListenerConcurrently {

    private final MessageListenerConcurrently messageListener;

    public MessageListenerConcurrentlyAdaptor(MessageListenerConcurrently messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        return messageListener.consumeMessage(msgs, context);
    }
}
