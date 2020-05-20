/**
 * Copyright
 */
package com.alpha.coding.common.event.handler;

import java.util.Set;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

/**
 * EventHandler 事件处理器
 *
 * @param <K> key类型
 * @param <E> 事件类别
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface EventHandler<K, E extends EnumWithCodeSupplier> extends HandlerIdentifier<E> {

    /**
     * 处理事件
     *
     * @param eventID 事件ID
     * @param keys    keys
     */
    void handle(String eventID, Set<K> keys);

}
