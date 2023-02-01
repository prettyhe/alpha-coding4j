package com.alpha.coding.common.event.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.aop.assist.AopHelper;
import com.alpha.coding.common.aop.assist.JoinPointContext;
import com.alpha.coding.common.aop.assist.SpelExpressionParserFactory;
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
import com.alpha.coding.common.spring.spel.GlobalExpressionCache;
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
public class EventMonitorAspect implements ApplicationContextAware {

    @Setter
    private ExpressionParser expressionParser = SpelExpressionParserFactory.getDefaultParser();
    @Setter
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    @Setter
    private ApplicationContext applicationContext;

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

    /**
     * 切面方法
     */
    public Object doAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        // 执行方法，获取返回的对象
        Object returnValue = joinPoint.proceed(joinPoint.getArgs());
        EventMonitor.List monitors = getAnnotation(joinPoint, EventMonitor.List.class);
        EventMonitor monitor = getAnnotation(joinPoint, EventMonitor.class);
        try {
            if (monitors != null) {
                List<EventMonitor> monitorList = Lists.newArrayList();
                monitorList.addAll(Arrays.asList(monitors.value()));
                if (monitor != null) {
                    monitorList.add(monitor);
                }
                processMulti(joinPoint, returnValue, monitorList);
            }
            if (monitor != null) {
                processSingle(joinPoint, returnValue, monitor);
            }
        } catch (Exception e) {
            log.error("event-monitor-aspect-error", e);
        }
        return returnValue;
    }

    /**
     * 获取method上面的注解信息
     */
    private <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Class<?> targetClass = AopHelper.getTargetClass(joinPoint.getTarget());
        final Method method = AopHelper.getTargetMethod(targetClass, signature.getMethod());
        return method.getAnnotation(annotationClass);
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
            if (!configuration.isEnablePublishEmptyEvent() && (keys == null || keys.isEmpty())) {
                return;
            }
            if (configuration.getAllowedPublishEventSet() != null
                    && !configuration.getAllowedPublishEventSet().contains(eventTypeObject)) {
                return;
            }
            AbstractEvent event = new AbstractEvent(eventTypeObject, keys, monitor.syncPost());
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

    private Set getKeysByExpression(JoinPoint joinPoint, Object returnValue, String[] keyExpressions) {
        if (keyExpressions == null || keyExpressions.length == 0) {
            return null;
        }
        final ParseSrcWrapper parseSrcWrapper = new ParseSrcWrapper();
        parseSrcWrapper.setReturnValue(returnValue);
        parseSrcWrapper.setArgs(joinPoint.getArgs());
        final JoinPointContext joinPointContext = new JoinPointContext(joinPoint);
        final Set keys = new HashSet();
        for (String expression : keyExpressions) {
            final Object value = AopHelper.evalSpELExpress(GlobalExpressionCache.getCache(),
                    joinPointContext.getMetadataCacheKey(), expression, expressionParser,
                    AopHelper.createMethodBasedWithResultEvaluationContext(joinPointContext.getMethod(),
                            joinPointContext.getArgs(), joinPointContext.getTarget(),
                            joinPointContext.getTargetClass(), returnValue,
                            applicationContext, parameterNameDiscoverer));
            if (value != null) {
                keys.add(value);
            }
        }
        return keys;
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
        } else if (keyFrom.equals(EventKeyFrom.EXPRESSION)) {
            return getKeysByExpression(joinPoint, returnValue, monitor.keyExpressions());
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
            if (b.contains(k)) {
                continue;
            }
            ret.add(k);
        }
        return ret;
    }

    private <K> Set<K> union(Set<K> a, Set<K> b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        Set<K> ret = Sets.newHashSet(a);
        ret.addAll(b);
        return ret;
    }

    private <K> Set<K> intersect(Set<K> a, Set<K> b) {
        if (a == null || b == null) {
            return null;
        }
        Set<K> ret = union(a, b);
        for (Iterator<K> it = ret.iterator(); it.hasNext(); ) {
            K k = it.next();
            if (a.contains(k) && b.contains(k)) {
                continue;
            }
            it.remove();
        }
        return ret;
    }

}
