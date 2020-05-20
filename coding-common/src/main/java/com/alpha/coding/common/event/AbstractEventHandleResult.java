/**
 * Copyright
 */
package com.alpha.coding.common.event;

import java.util.Set;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AbstractEventHandleResult
 *
 * @param <K> KEY类型
 * @param <E> 事件异常类型
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public abstract class AbstractEventHandleResult<K, E extends EnumWithCodeSupplier> {

    private E errType;
    private String msg;
    private Set<K> keys;

    public String toMsg() {
        return new StringBuilder()
                .append("errType=").append(errType)
                .append(", keys=").append(keys)
                .append(", msg=").append(msg)
                .toString();
    }

}
