package com.alpha.coding.common.message.publish;

import com.alpha.coding.common.message.config.MessagePublishConfig;

/**
 * MessagePublishAdaptor
 *
 * @version 1.0
 * Date: 2021/9/8
 */
public interface MessagePublishAdaptor {

    /**
     * 发送消息
     *
     * @param topic         消息主题
     * @param tag           消息标签
     * @param content       消息内容
     * @param bizNo         业务标识
     * @param publishConfig 发送配置
     * @return 发送完成后的消息ID
     */
    String send(String topic, String tag, String content, String bizNo, MessagePublishConfig publishConfig);
}
