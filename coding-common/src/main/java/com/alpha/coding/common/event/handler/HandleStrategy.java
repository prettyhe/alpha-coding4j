/**
 * Copyright
 */
package com.alpha.coding.common.event.handler;

import java.util.List;
import java.util.Set;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.AbstractEventHandleResult;

/**
 * HandleStrategy 事件处理策略
 *
 * @param <K> key类型
 * @param <R> 处理结果类型
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface HandleStrategy<K, R extends EnumWithCodeSupplier> {

    /**
     * 处理策略，返回处理结果
     *
     * @param keys keys
     *
     * @return 处理结果集
     */
    List<? extends AbstractEventHandleResult<K, R>> handleWithStrategy(Set<K> keys);

}
