/**
 * Copyright
 */
package com.alpha.coding.common.event.listener;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.AbstractEvent;
import com.alpha.coding.common.event.EventIdentifier;

/**
 * EventListener 事件监听器
 *
 * @param <E> 事件类别
 * @version 1.0
 * Date: 2020-02-21
 */
public interface EventListener<E extends EnumWithCodeSupplier> extends EventIdentifier, java.util.EventListener {

    /**
     * 监听事件
     *
     * @param event 事件
     * @param <K>   key类型
     * @param <AE>  事件类别
     */
    <K, AE extends AbstractEvent<K, E>> void listen(AE event);

}
