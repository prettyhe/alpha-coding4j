package com.alpha.coding.common.message.publish.impl;

import com.alpha.coding.common.message.dal.MessageMonitor;
import com.alpha.coding.common.message.publish.MessagePublishListener;

import lombok.extern.slf4j.Slf4j;

/**
 * LogMessagePublishListener
 *
 * @version 1.0
 * Date: 2021/7/22
 */
@Slf4j
public class LogMessagePublishListener implements MessagePublishListener {

    private static final LogMessagePublishListener INSTANCE = new LogMessagePublishListener();

    public static LogMessagePublishListener getInstance() {
        return INSTANCE;
    }

    @Override
    public void onSuccess(MessageMonitor monitor) {
        log.info("消息发送成功:topic={},tag={},content={},bizNo={},msgId={}",
                monitor.getTopic(), monitor.getTag(), monitor.getContent(), monitor.getBizNo(), monitor.getMsgId());
    }

    @Override
    public void onFail(MessageMonitor monitor, Throwable throwable) {
        log.error("P3|消息发送失败|{}|{}|id={},bizNo={},content={},errMsg={}",
                monitor.getTopic(), monitor.getTag(), monitor.getBizNo(), monitor.getContent(),
                throwable == null ? null : throwable.getMessage(), throwable);
    }
}
