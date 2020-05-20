package com.alpha.coding.example.event;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.alpha.coding.common.event.AbstractEventHandleResult;
import com.alpha.coding.common.event.handler.CallbackEventHandlerTemplate;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * LoanChangeEventHandler
 *
 * @version 1.0
 * Date: 2020-02-19
 */
@Slf4j
@Component
public class DataAChangeEventHandler
        extends CallbackEventHandlerTemplate<Object, CacheEventType, CacheEventErrorType> {

    @Override
    public List<? extends AbstractEventHandleResult<Object, CacheEventErrorType>> handleWithStrategy(Set<Object> keys) {
        // TODO 执行业务逻辑，如刷新缓存
        return Lists.newArrayList();
    }

    @Override
    public CacheEventType getEventType() {
        return CacheEventType.DATA_A_CHANGE;
    }

}
