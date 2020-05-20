package com.alpha.coding.example.event;

import java.util.Set;

import com.alpha.coding.common.event.AbstractEventHandleResult;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CacheEventResult
 *
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CacheEventResult<K> extends AbstractEventHandleResult<K, CacheEventErrorType> {

    public static <KEY> CacheEventResult<KEY> genResult(CacheEventErrorType type, Set<KEY> keys, String msg) {
        CacheEventResult<KEY> ret = new CacheEventResult<>();
        ret.setErrType(type);
        ret.setKeys(keys);
        ret.setMsg(msg);
        return ret;
    }

}
