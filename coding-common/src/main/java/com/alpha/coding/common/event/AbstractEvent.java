/**
 * Copyright
 */
package com.alpha.coding.common.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * AbstractEvent 事件抽象
 * <p>
 * 事件抽象为类型+一组key的组合，表示需要将一组数传递给下一个handler进行处理;
 * 每个事件都有一个唯一标识，在事件产生时随之确定，并记录到log用于定位
 * </p>
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class AbstractEvent<K, E extends EnumWithCodeSupplier> {

    private static final String UNDER_LINE = "_";
    private static final String MIDDLE_LINE = "-";
    public static final String MDC_CONTEXT = "_MDC_CONTEXT";
    public static final String MTLA_CONTEXT = "_MTLA_CONTEXT";

    @Getter
    private final E type;

    @Getter
    private final ImmutableSet<K> keys;

    @Getter
    private final long timestamp = System.currentTimeMillis();

    @Getter
    private final String eventID;

    @Getter
    private final Map<String, Object> context = new HashMap<>();

    public AbstractEvent(E type, Collection<K> keys) {
        this.type = type;
        if (keys == null) {
            this.keys = null;
        } else {
            this.keys = ImmutableSet.copyOf(keys);
        }
        final Map<String, String> mdcCopyOfContextMap = MDC.getCopyOfContextMap();
        if (mdcCopyOfContextMap != null) {
            context.put(MDC_CONTEXT, mdcCopyOfContextMap);
        }
        final Map<String, Object> mapThreadLocalAdaptorCopyOfContextMap = MapThreadLocalAdaptor.getCopyOfContextMap();
        if (mapThreadLocalAdaptorCopyOfContextMap != null) {
            context.put(MTLA_CONTEXT, mapThreadLocalAdaptorCopyOfContextMap);
        }
        this.eventID = genEventID();
        if (log.isDebugEnabled()) {
            log.debug("event-generate: eventID={}, event={}", eventID, JSON.toJSONString(this));
        }
    }

    private String genEventID() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getClass().getSimpleName())
                .append(MIDDLE_LINE)
                .append(type)
                .append(UNDER_LINE);
        if (keys != null && !keys.isEmpty()) {
            sb.append(Objects.hashCode(JSON.toJSONString(keys)));
        } else {
            sb.append("0");
        }
        return sb.toString();
    }

}
