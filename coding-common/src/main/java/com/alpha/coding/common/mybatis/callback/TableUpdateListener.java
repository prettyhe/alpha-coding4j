package com.alpha.coding.common.mybatis.callback;

import com.alpha.coding.common.mybatis.common.TableUpdateDto;

/**
 * TableUpdateListener
 *
 * @version 1.0
 * Date: 2021/4/13
 */
public interface TableUpdateListener {

    /**
     * 更细前回调
     */
    default void beforeUpdate(TableUpdateDto dto) {
        // nothing
    }

    /**
     * 监听更新事件
     */
    void onUpdate(TableUpdateDto dto);
}
