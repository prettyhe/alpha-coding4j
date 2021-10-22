package com.alpha.coding.common.message.publish;

import com.alpha.coding.common.message.config.MessagePublishConfig;
import com.alpha.coding.common.message.dal.MessageMonitor;

/**
 * MessagePublisher
 *
 * @version 1.0
 * Date: 2021/7/3
 */
public interface MessagePublisher {

    /**
     * 同步发送
     *
     * @param topic   消息主题
     * @param tag     消息标签
     * @param content 消息内容
     * @param bizNo   业务标识
     * @return 发送完成后的消息ID
     */
    String syncSend(String topic, String tag, String content, String bizNo, MessagePublishConfig config);

    /**
     * 发送消息
     *
     * @param topic   消息主题
     * @param tag     消息标签
     * @param content 消息内容
     * @param bizNo   业务标识
     * @param config  发送控制
     */
    void asyncSend(String topic, String tag, String content, String bizNo, MessagePublishConfig config);

    /**
     * 从监控记录发送
     *
     * @param messageMonitor 监控记录
     * @return 发送完成后的消息ID
     */
    String sendFromMonitor(MessageMonitor messageMonitor);

}
