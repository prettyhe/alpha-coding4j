package com.alpha.coding.common.mybatis.callback;

/**
 * RecordUpdateStub
 *
 * @version 1.0
 * Date: 2021/9/16
 */
public interface RecordUpdateStub {

    /**
     * 通过主键获取数据
     */
    Object selectByPrimaryKey(Long primaryKey);

}
