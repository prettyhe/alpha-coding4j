package com.alpha.coding.common.message.task;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.executor.schedule.ScheduledTask;
import com.alpha.coding.bo.function.common.Functions;
import com.alpha.coding.bo.trace.TimestampBase62UUIDTraceIdGenerator;
import com.alpha.coding.common.bean.init.AsyncWarmUpCallback;
import com.alpha.coding.common.function.FunctionDelegator;
import com.alpha.coding.common.message.DependencyHolder;
import com.alpha.coding.common.message.constant.MessageSendStatus;
import com.alpha.coding.common.message.dal.MessageMonitor;
import com.alpha.coding.common.message.dal.MessageMonitorDao;
import com.alpha.coding.common.message.publish.impl.MQMessagePublishDelegate;
import com.alpha.coding.common.redis.RedisTemplateUtils;
import com.alpha.coding.common.utils.DateUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * MessagePublishCompensateTask
 *
 * @version 1.0
 * Date: 2021/9/8
 */
@Slf4j
public class MessagePublishCompensateTask implements AsyncWarmUpCallback, DisposableBean {

    private static final String MIN_NEXT_SEND_TIME_KEY = "MQM:min_next_send_time";
    private static final String MIN_NEXT_SEND_TIME_CURSOR_KEY = "MQM:CUR:min_next_send_time";

    @Value("${message.compensate.task.interval:120}")
    private long messageCompensateTaskInterval;

    @Setter
    private volatile boolean synchronizedExecTask = false;

    @Setter
    private volatile boolean running = true;

    @Autowired
    private DependencyHolder dependencyHolder;

    @Autowired
    private MessageMonitorDao messageMonitorDao;

    @Override
    public List<Runnable> asyncWarmUp() {
        running = true;
        return Collections.singletonList(new ScheduledTask()
                .setInitialDelay(1)
                .setPeriod(messageCompensateTaskInterval)
                .setTimeUnit(TimeUnit.SECONDS)
                .setScheduledMode(ScheduledTask.ScheduledMode.FixedRate)
                .setCommand(t -> FunctionDelegator.traceRun(() -> {
                    if (!running) {
                        log.warn("[业务组{}]消息发送补偿任务已终止", dependencyHolder.bizGroup());
                        return;
                    }
                    if (!synchronizedExecTask) {
                        execute(t);
                    } else {
                        final Tuple<Boolean, Object> tuple = RedisTemplateUtils
                                .doInLockAutoRenewalReturnOnLockFail(dependencyHolder.getRedisTemplate(),
                                        dependencyHolder.bizGroup() + ":MQM:MessagePublishCompensateTask", 10,
                                        () -> {
                                            execute(t);
                                            return null;
                                        });
                        if (!tuple.getF()) {
                            log.info("[业务组{}]消息发送补偿任务执行失败，未获取到锁", dependencyHolder.bizGroup());
                        }
                    }
                }, TimestampBase62UUIDTraceIdGenerator.getInstance())));
    }

    /**
     * 任务执行逻辑
     */
    private void execute(ScheduledTask task) {
        final String bizGroup = dependencyHolder.bizGroup();
        log.info("[业务组{}]消息发送补偿任务开始执行", bizGroup);
        final long startNanoTime = System.nanoTime();
        Date st = null;
        Long minId = null;
        try {
            Date newDate = loadMinNextSendTimeFromCache();
            if (newDate == null) {
                newDate = loadMinNextSendTime();
            }
            st = upsertMinNextSendTime(newDate);
            final Set<Long> existIds = new HashSet<>(); // 上一轮ID
            while (true) {
                final List<MessageMonitor> list =
                        messageMonitorDao.selectSinceNextSendTime(st,
                                MessageSendStatus.WAIT_SEND.getCode(), minId, 20);
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }
                for (MessageMonitor messageMonitor : list) {
                    if (existIds.contains(messageMonitor.getId())) {
                        continue;
                    }
                    try {
                        MQMessagePublishDelegate.messagePublisher().sendFromMonitor(messageMonitor);
                    } catch (Exception e) {
                        log.info("[业务组{}]消息补偿处理失败,messageMonitor={}",
                                bizGroup, JSON.toJSONString(messageMonitor));
                    }
                }
                existIds.clear();
                list.forEach(p -> existIds.add(p.getId()));
                final Date nextSendTime = list.get(list.size() - 1).getNextSendTime();
                // 时间是同一秒，则使用id作为游标
                if (DateUtils.format(st, DateUtils.DEFAULT_FORMAT)
                        .equals(DateUtils.format(nextSendTime, DateUtils.DEFAULT_FORMAT))) {
                    minId = list.get(list.size() - 1).getId();
                } else {
                    // 时间有变化，使用时间作为游标
                    minId = null;
                    st = upsertMinNextSendTime(nextSendTime);
                }
            }
            log.info("[业务组{}]消息发送补偿任务执行成功，耗时{}，下次发送时间{}", bizGroup,
                    Functions.formatNanos.apply(System.nanoTime() - startNanoTime),
                    DateUtils.format(st));
        } catch (Throwable e) {
            log.info("[业务组{}]消息发送补偿任务执行失败，耗时{}，下次发送时间{}", bizGroup,
                    Functions.formatNanos.apply(System.nanoTime() - startNanoTime),
                    DateUtils.format(st), e);
        }
    }

    private Date loadMinNextSendTimeFromCache() {
        return Optional.ofNullable(dependencyHolder.getRedisTemplate().opsForValue()
                .get(dependencyHolder.bizGroup() + ":" + MIN_NEXT_SEND_TIME_KEY))
                .map(String::valueOf).map(DateUtils::smartParse).orElse(null);
    }

    private Date loadMinNextSendTime() {
        final Date date = Optional.ofNullable(messageMonitorDao.selectMinNextSendTime()).orElse(new Date());
        final String key = dependencyHolder.bizGroup() + ":" + MIN_NEXT_SEND_TIME_KEY;
        dependencyHolder.getRedisTemplate().opsForValue().set(key, DateUtils.format(date));
        dependencyHolder.getRedisTemplate().expire(key, 10L, TimeUnit.MINUTES);
        return date;
    }

    private Date upsertMinNextSendTime(Date newDate) {
        final ValueOperations valueOperations = dependencyHolder.getRedisTemplate().opsForValue();
        final String key = dependencyHolder.bizGroup() + ":" + MIN_NEXT_SEND_TIME_CURSOR_KEY;
        Object val = valueOperations.get(key);
        if (val == null) {
            Object value = dependencyHolder.getRedisTemplate().opsForValue()
                    .get(dependencyHolder.bizGroup() + ":" + MIN_NEXT_SEND_TIME_KEY);
            if (value == null) {
                value = loadMinNextSendTime();
                valueOperations.set(key, DateUtils.format((Date) value));
                return (Date) value;
            }
            val = value;
        }
        Date cursor = DateUtils.smartParse(String.valueOf(val));
        if (newDate == null) {
            return cursor;
        }
        if (newDate.after(cursor)) {
            valueOperations.set(key, DateUtils.format(newDate));
        }
        return newDate;
    }

    @Override
    public void destroy() throws Exception {
        running = false;
    }

}
