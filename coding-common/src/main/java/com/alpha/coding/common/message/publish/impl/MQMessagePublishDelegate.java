package com.alpha.coding.common.message.publish.impl;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.function.common.Predicates;
import com.alpha.coding.bo.trace.HalfMDCTraceIdGenerator;
import com.alpha.coding.common.function.FunctionDelegator;
import com.alpha.coding.common.message.DependencyHolder;
import com.alpha.coding.common.message.config.MessagePublishConfig;
import com.alpha.coding.common.message.constant.MessageSendStatus;
import com.alpha.coding.common.message.dal.MessageMonitor;
import com.alpha.coding.common.message.dal.MessageMonitorDao;
import com.alpha.coding.common.message.event.MessageApplicationEvent;
import com.alpha.coding.common.message.publish.MessagePublishListener;
import com.alpha.coding.common.message.publish.MessagePublisher;
import com.alpha.coding.common.redis.RedisTemplateUtils;
import com.alpha.coding.common.utils.MD5Utils;

import lombok.extern.slf4j.Slf4j;

/**
 * MQMessagePublishDelegate
 *
 * @version 1.0
 * Date: 2021/7/3
 */
@Slf4j
@Component("messagePublishDelegate")
public class MQMessagePublishDelegate implements MessagePublisher, MessagePublishListener,
        BeanNameAware, ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static String beanName;

    @Autowired
    protected DependencyHolder dependencyHolder;

    @Autowired
    protected MessageMonitorDao messageMonitorDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Value("${default.message.publish.retry.interval:10}")
    private int defaultMessagePublishRetryInterval;

    /**
     * 获取容器中发送器bean实例
     */
    public static MessagePublisher messagePublisher() {
        return applicationContext.getBean(beanName, MessagePublisher.class);
    }

    @Override
    public void setBeanName(String beanName) {
        MQMessagePublishDelegate.beanName = beanName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MQMessagePublishDelegate.applicationContext = applicationContext;
    }

    @Override
    public void onSuccess(MessageMonitor monitor) {
        LogMessagePublishListener.getInstance().onSuccess(monitor);
    }

    @Override
    public void onFail(MessageMonitor monitor, Throwable throwable) {
        LogMessagePublishListener.getInstance().onFail(monitor, throwable);
    }

    /**
     * 构建消息监控对象
     */
    private MessageMonitor buildMessageMonitor(String topic, String tag, String content, String bizNo,
                                               MessagePublishConfig config) {
        final MessageMonitor record = new MessageMonitor();
        record.setTopic(topic);
        record.setTag(tag);
        record.setContent(content);
        record.setBizNo(bizNo != null ? bizNo
                : (content != null ? MD5Utils.md5(content) : UUID.randomUUID().toString().replaceAll("-", "")));
        if (config != null && config.getDelaySendTime() != null) {
            record.setNextSendTime(config.getDelaySendTime());
        } else {
            record.setNextSendTime(new Date());
        }
        record.setStatus(MessageSendStatus.WAIT_SEND.getCode()); // 0-待发送,1-成功,2-失败,3-待回执,4-取消
        if (config != null) {
            record.setMaxTryTimes(config.getMaxTryTimes());
            record.setRetryInterval(config.getRetryInterval());
            record.setNeedReceipt(config.isNeedReceipt() ? 1 : 0);
            record.setPublishListener(config.getPublishListener());
        }
        if (record.getPublishListener() == null) {
            record.setPublishListener(beanName);
        }
        record.setPublishConfig(config);
        final Object tenantId = MapThreadLocalAdaptor.get("tenantId");
        if (tenantId != null) {
            try {
                record.setTenantId(Integer.valueOf(String.valueOf(tenantId)));
            } catch (NumberFormatException e) {
                log.warn("parse tenantId fail, tenantId={}", tenantId);
            }
        }
        return record;
    }

    @Override
    public String syncSend(String topic, String tag, String content, String bizNo, MessagePublishConfig config) {
        final MessageMonitor messageMonitor = buildMessageMonitor(topic, tag, content, bizNo, config);
        return sendFromMonitor(messageMonitor);
    }

    @Override
    public void asyncSend(String topic, String tag, String content, String bizNo, MessagePublishConfig config) {
        final MessageMonitor messageMonitor = buildMessageMonitor(topic, tag, content, bizNo, config);
        messageMonitorDao.insertSelective(messageMonitor);
        applicationEventPublisher.publishEvent(new MessageApplicationEvent(messageMonitor));
    }

    @Override
    public String sendFromMonitor(MessageMonitor messageMonitor) {
        final String lockKey = dependencyHolder.bizGroup() + ":MESSAGE:RLK:MQPUB:" + messageMonitor.getBizNo();
        final Tuple<Boolean, String> tuple = RedisTemplateUtils
                .doInLockAutoRenewalReturnOnLockFail(dependencyHolder.getRedisTemplate(), lockKey,
                        5, () -> {
                            // 幂等检查
                            if (messageMonitor.getId() != null) {
                                final MessageMonitor monitor =
                                        messageMonitorDao.selectByPrimaryKey(messageMonitor.getId());
                                if ((monitor.getNextSendTime() != null
                                             && monitor.getNextSendTime().compareTo(new Date()) > 0)
                                        || !Predicates.testIntEqual.test(messageMonitor.getStatus(),
                                        MessageSendStatus.WAIT_SEND.getCode())) {
                                    // 未到发送时间，或非待发送状态，不处理
                                    return monitor.getMsgId();
                                }
                                return doSendMessageMonitor(monitor);
                            } else {
                                return doSendMessageMonitor(messageMonitor);
                            }
                        });
        if (!tuple.getF()) {
            log.warn("未获取到分布式锁:{},{}", JSON.toJSONString(messageMonitor), lockKey);
        }
        return tuple.getS();
    }

    /**
     * 发送逻辑
     */
    private String doSendMessageMonitor(MessageMonitor messageMonitor) {
        String msgId = null;
        int tryTimes = Optional.ofNullable(messageMonitor.getTryTimes()).orElse(0) + 1;
        final MessagePublishListener publishListener = applicationContext
                .getBean(messageMonitor.getPublishListener(), MessagePublishListener.class);
        final Object oldTenantId = MapThreadLocalAdaptor.get("tenantId");
        try {
            if (messageMonitor.getTenantId() != null) {
                MapThreadLocalAdaptor.put("tenantId", messageMonitor.getTenantId());
            }
            msgId = dependencyHolder.getMessagePublishAdaptor().send(messageMonitor.getTopic(),
                    messageMonitor.getTag(), messageMonitor.getContent(),
                    messageMonitor.getBizNo(), messageMonitor.getPublishConfig());
            messageMonitor.setMsgId(msgId);
            // 需要回执
            if (messageMonitor.getNeedReceipt() != null
                    && Predicates.testIntValue.test(messageMonitor.getNeedReceipt(), 1)) {
                messageMonitor.setStatus(MessageSendStatus.WAIT_RECEIPT.getCode()); // 待回执
            } else {
                messageMonitor.setStatus(MessageSendStatus.SUCCESS.getCode()); // 成功
            }
            messageMonitor.setSendTime(new Date());
            messageMonitor.setTryTimes(tryTimes);
            if (publishListener != null) {
                publishListener.onSuccess(messageMonitor);
            }
        } catch (Throwable e) {
            messageMonitor.setSendTime(new Date());
            messageMonitor.setTryTimes(tryTimes);
            // 无限重试模式
            if (messageMonitor.getMaxTryTimes() == null
                    || Predicates.testIntValue.test(messageMonitor.getMaxTryTimes(), -1)) {
                messageMonitor.setNextSendTime(new Date(new Date().getTime()
                        + defaultMessagePublishRetryInterval * 1000L));
            } else {
                if (messageMonitor.getMaxTryTimes() <= tryTimes) {
                    messageMonitor.setStatus(MessageSendStatus.CANCEL.getCode()); // 达到最大重试次数，任务取消
                } else {
                    messageMonitor.setNextSendTime(new Date(new Date().getTime()
                            + defaultMessagePublishRetryInterval * 1000L));
                }
            }
            if (publishListener != null) {
                publishListener.onFail(messageMonitor, e);
            }
        } finally {
            if (messageMonitor.getId() == null) {
                messageMonitorDao.insertSelective(messageMonitor);
            } else {
                messageMonitorDao.updateByPrimaryKeySelective(messageMonitor);
            }
            MapThreadLocalAdaptor.put("tenantId", oldTenantId);
        }
        return msgId;
    }

    @Async("messageApplicationEventHandlerExec")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT,
            value = MessageApplicationEvent.class, fallbackExecution = true)
    public void handleMessageApplicationEvent(MessageApplicationEvent event) {
        FunctionDelegator.traceRun(() -> {
            try {
                final MessageMonitor messageMonitor = (MessageMonitor) event.getSource();
                if (messageMonitor == null) {
                    log.warn("无消息记录");
                    return;
                }
                if (messageMonitor.getNextSendTime().after(new Date())) {
                    return; // 未到发送时间
                }
                sendFromMonitor(messageMonitor);
            } catch (Throwable e) {
                log.error("监听事务提交发送消息事件处理异常：{}", JSON.toJSONString(event.getSource()), e);
            }
        }, HalfMDCTraceIdGenerator.getTimestampBase62Instance());
    }

}
