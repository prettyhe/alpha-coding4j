/**
 * Copyright
 */
package com.alpha.coding.common.event.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.aop.assist.AopHelper;
import com.alpha.coding.common.event.AbstractEvent;
import com.alpha.coding.common.event.annotations.EventMonitor;
import com.alpha.coding.common.event.annotations.EventType;
import com.alpha.coding.common.event.configuration.EventConfiguration;
import com.alpha.coding.common.event.configuration.EventConfigurationFactory;
import com.alpha.coding.common.event.eventbus.EventBusFactory;
import com.alpha.coding.common.event.parser.EventKeyFrom;
import com.alpha.coding.common.event.parser.EventKeyParser;
import com.alpha.coding.common.event.parser.EventKeyParserFactory;
import com.alpha.coding.common.event.parser.ParseSrcWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * EventMonitorAspect
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Aspect
@Component
public class EventMonitorAspect {

    @Setter
    @Autowired
    private EventKeyParserFactory eventKeyParserFactory;

    @Setter
    @Autowired
    private EventBusFactory eventBusFactory;

    @Setter
    @Autowired
    private EventConfigurationFactory eventConfigurationFactory;

    /**
     * 解析事件类型解析器缓存
     */
    private ConcurrentMap<EventAnnotatedElementKey, BiFunction<Class<? extends EnumWithCodeSupplier>, String,
            EnumWithCodeSupplier>> eventTypeParseCache = new ConcurrentHashMap<>(64);

    @Pointcut("@annotation(com.alpha.coding.common.event.annotations.EventMonitor) || "
            + "@annotation(com.alpha.coding.common.event.annotations.EventMonitor.List)")
    public void eventMonitorAspect() {
        // point cut
    }

    @Around("eventMonitorAspect()")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
        // 执行方法，获取返回的对象
        Object returnValue = joinPoint.proceed(joinPoint.getArgs());
        EventMonitor.List monitors = getAnnotation(joinPoint, EventMonitor.List.class);
        EventMonitor monitor = getAnnotation(joinPoint, EventMonitor.class);
        try {
            if (monitors != null) {
                List<EventMonitor> monitorList = Lists.newArrayList();
                for (EventMonitor mon : monitors.value()) {
                    monitorList.add(mon);
                }
                if (monitor != null) {
                    monitorList.add(monitor);
                }
                processMulti(joinPoint, returnValue, monitorList);
            } else {
                if (monitor != null) {
                    processSingle(joinPoint, returnValue, monitor);
                }
            }
        } catch (Exception e) {
            log.error("event-monitor-aspect-error", e);
        }
        return returnValue;
    }

    /**
     * 获取method上面的注解信息
     */
    private <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> annotationClass)
            throws ClassNotFoundException {
        // 获取目标类名
        String targetName = joinPoint.getTarget().getClass().getName();
        // 获取方法名
        String methodName = joinPoint.getSignature().getName();
        // 生成类对象
        Class targetClass = Class.forName(targetName);
        // 获取该类中的方法
        Method[] methods = targetClass.getMethods();
        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            return method.getAnnotation(annotationClass);
        }
        return null;
    }

    private void processSingle(final ProceedingJoinPoint joinPoint, final Object returnValue,
                               final EventMonitor monitor) {
        try {
            Set keys = getFinalKeys(joinPoint, returnValue, monitor);
            final EventType eventType = monitor.eventType();
            final Class<?> targetClass = AopHelper.getTargetClass(joinPoint.getTarget());
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(signature.getMethod(), targetClass);
            EventAnnotatedElementKey elementKey =
                    new EventAnnotatedElementKey(annotatedElementKey, eventType.eventClass(), eventType.type());
            final BiFunction<Class<? extends EnumWithCodeSupplier>, String, EnumWithCodeSupplier>
                    eventTypeParser = eventTypeParseCache.computeIfAbsent(elementKey, p -> {
                try {
                    return eventType.typeParser().newInstance();
                } catch (Exception e) {
                    log.warn("cannot get eventTypeParser instance for {}", elementKey);
                }
                return null;
            });
            if (eventTypeParser == null) {
                return;
            }
            final EnumWithCodeSupplier eventTypeObject =
                    eventTypeParser.apply(eventType.eventClass(), eventType.type());
            if (eventTypeObject == null) {
                log.warn("null eventTypeObject for {}", elementKey);
                return;
            }
            EventConfiguration configuration =
                    eventConfigurationFactory.getEventConfiguration(eventType.eventClass(), eventTypeObject);
            if (!configuration.isEnablePublishEmptyEvent() && CollectionUtils.isEmpty(keys)) {
                return;
            }
            if (configuration.getAllowedPublishEventSet() != null
                    && !configuration.getAllowedPublishEventSet().contains(eventType)) {
                return;
            }
            AbstractEvent event = new AbstractEvent(eventTypeObject, keys);
            eventBusFactory.post(event);
        } catch (Exception e) {
            log.error("process-event-pointCut error, monitor={}", JSON.toJSONString(monitor), e);
        }
    }

    private void processMulti(final ProceedingJoinPoint joinPoint, final Object returnValue,
                              final List<EventMonitor> monitors) {
        for (EventMonitor monitor : monitors) {
            processSingle(joinPoint, returnValue, monitor);
        }
    }

    private Set getKeysFromRequest(JoinPoint joinPoint, EventMonitor monitor) {
        Object[] args = joinPoint.getArgs();
        int keyOrder = monitor.keyParamOrder();
        if (args == null || keyOrder < 0 || args.length < keyOrder || args[keyOrder] == null) {
            throw new RuntimeException("cannot parse keys from null request params or wrong keyOrder");
        }
        Class<? extends EventKeyParser> parserClazz = monitor.requestKeyParser();
        EventKeyParser parser = eventKeyParserFactory.getParser(parserClazz);
        if (parser != null) {
            return parser.parse(args[keyOrder]);
        }
        throw new RuntimeException("unknown EventKeyParser: " + parserClazz.getName());
    }

    private Set getKeysFromReturn(Object returnValue, EventMonitor monitor) {
        if (returnValue == null) {
            return Sets.newHashSet();
        }
        Class<? extends EventKeyParser> parserClazz = monitor.returnKeyParser();
        EventKeyParser parser = eventKeyParserFactory.getParser(parserClazz);
        if (parser != null) {
            return parser.parse(returnValue);
        }
        throw new RuntimeException("unknown EventKeyParser: " + parserClazz.getName());
    }

    private Set getKeysByCustom(JoinPoint joinPoint, Object returnValue, EventMonitor monitor) {
        Object[] args = joinPoint.getArgs();
        Class<? extends EventKeyParser> parserClazz = monitor.customKeyParser();
        EventKeyParser parser = eventKeyParserFactory.getParser(parserClazz);
        if (parser != null) {
            ParseSrcWrapper parseSrcWrapper = new ParseSrcWrapper();
            parseSrcWrapper.setReturnValue(returnValue);
            parseSrcWrapper.setArgs(args);
            return parser.parse(parseSrcWrapper);
        }
        throw new RuntimeException("unknown EventKeyParser: " + parserClazz.getName());
    }

    private Set getKeysByRequestReturnWrapper(JoinPoint joinPoint, Object returnValue) {
        ParseSrcWrapper parseSrcWrapper = new ParseSrcWrapper();
        parseSrcWrapper.setReturnValue(returnValue);
        parseSrcWrapper.setArgs(joinPoint.getArgs());
        return Sets.newHashSet(parseSrcWrapper);
    }

    private Set getFinalKeys(final ProceedingJoinPoint joinPoint, final Object returnValue, EventMonitor monitor) {
        EventKeyFrom keyFrom = monitor.keyFrom();
        if (keyFrom.equals(EventKeyFrom.NONE)) {
            return null;
        } else if (keyFrom.equals(EventKeyFrom.REQUEST)) {
            return getKeysFromRequest(joinPoint, monitor);
        } else if (keyFrom.equals(EventKeyFrom.RETURN)) {
            return getKeysFromReturn(returnValue, monitor);
        } else if (keyFrom.equals(EventKeyFrom.REQUEST_MINUS_RETURN)) {
            return minus(getKeysFromRequest(joinPoint, monitor), getKeysFromReturn(returnValue, monitor));
        } else if (keyFrom.equals(EventKeyFrom.REQUEST_UNION_RETURN)) {
            return union(getKeysFromRequest(joinPoint, monitor), getKeysFromReturn(returnValue, monitor));
        } else if (keyFrom.equals(EventKeyFrom.REQUEST_INTERSECT_RETURN)) {
            return intersect(getKeysFromRequest(joinPoint, monitor), getKeysFromReturn(returnValue, monitor));
        } else if (keyFrom.equals(EventKeyFrom.CUSTOM)) {
            return getKeysByCustom(joinPoint, returnValue, monitor);
        } else if (keyFrom.equals(EventKeyFrom.REQUEST_RETURN_WRAPPER)) {
            return getKeysByRequestReturnWrapper(joinPoint, returnValue);
        }
        throw new RuntimeException("unknown keyFrom type: " + monitor.keyFrom());
    }

    private <K> Set<K> minus(Set<K> a, Set<K> b) {
        if (a == null) {
            throw new RuntimeException("null Set");
        }
        if (b == null) {
            return a;
        }
        Set<K> ret = Sets.newHashSet();
        for (K k : a) {
            if (b.contains(a)) {
                continue;
            }
            ret.add(k);
        }
        return ret;
    }

    private Set union(Set a, Set b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        Set ret = Sets.newHashSet(a);
        ret.addAll(b);
        return ret;
    }

    private <K> Set<K> intersect(Set<K> a, Set<K> b) {
        if (a == null || b == null) {
            return null;
        }
        Set<K> ret = union(a, b);
        for (Iterator iterator = ret.iterator(); iterator.hasNext(); ) {
            K k = (K) iterator.next();
            if (a.contains(k) && b.contains(k)) {
                continue;
            }
            iterator.remove();
        }
        return ret;
    }

}
