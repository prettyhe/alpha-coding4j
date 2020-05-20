package com.alpha.coding.example.service;

import org.springframework.stereotype.Component;

import com.alpha.coding.common.event.annotations.EventMonitor;
import com.alpha.coding.common.event.annotations.EventType;
import com.alpha.coding.common.event.parser.EventKeyFrom;
import com.alpha.coding.example.event.CacheEventType;

import lombok.extern.slf4j.Slf4j;

/**
 * DataAService
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Slf4j
@Component
public class DataAService {

    @EventMonitor(eventType = @EventType(eventClass = CacheEventType.class,
            type = "DATA_A_CHANGE"), keyFrom = EventKeyFrom.REQUEST)
    public void changeDataAToDB(Object dataA) {

    }

}
