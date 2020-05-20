/**
 * Copyright
 */
package com.alpha.coding.common.event.parser;

import java.util.Set;

/**
 * EventKeyParser 事件key解析器
 *
 * @param <K> key类型
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface EventKeyParser<K> {

    /**
     * 解析出key
     *
     * @param obj 参数
     *
     * @return key集合
     */
    Set<K> parse(Object obj);

}
