package com.alpha.coding.common.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alpha.coding.common.event.common.EventBusChangeEvent;
import com.alpha.coding.common.event.common.ListenerChangeEvent;
import com.alpha.coding.common.event.eventbus.EventBusFactory;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;

/**
 * EventBusUpdateListener
 *
 * @version 1.0
 * Date: 2020-02-19
 */
@Slf4j
@Component
public class MetaChangeListener {

    @Autowired
    private EventBusFactory eventBusFactory;

    @Autowired
    private EventListenerFactory eventListenerFactory;

    @Subscribe
    @AllowConcurrentEvents
    public void listenBusUpdate(EventBusChangeEvent event) {
        log.info("EventBusChangeEvent: {}", event);
        eventBusFactory.updateEventBusMap();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void listenListenerUpdate(ListenerChangeEvent event) {
        log.info("ListenerChangeEvent: {}", event);
        eventListenerFactory.updateListenerMap();
    }

}
