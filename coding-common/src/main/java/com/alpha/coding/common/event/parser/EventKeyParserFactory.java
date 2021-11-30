package com.alpha.coding.common.event.parser;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import lombok.Setter;

/**
 * EventKeyParserFactory 事件key解析器工厂，自动装配解析器
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Component
public class EventKeyParserFactory implements ApplicationContextAware, InitializingBean {

    @Setter
    private ApplicationContext applicationContext;

    private Map<Class<? extends EventKeyParser>, EventKeyParser> map = Maps.newHashMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventKeyParser> beans = applicationContext.getBeansOfType(EventKeyParser.class);
        for (EventKeyParser parser : beans.values()) {
            map.put(parser.getClass(), parser);
        }
    }

    public EventKeyParser getParser(Class<? extends EventKeyParser> type) {
        return map.get(type);
    }

}
