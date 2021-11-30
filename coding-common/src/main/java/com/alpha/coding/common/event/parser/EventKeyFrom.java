package com.alpha.coding.common.event.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * EventKeyFrom 事件key来源枚举
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Getter
@AllArgsConstructor
public enum EventKeyFrom {

    /**
     * 无
     */
    NONE(0, "无"),

    /**
     * 来源于参数
     */
    REQUEST(1, "请求参数"),

    /**
     * 来源于结果
     */
    RETURN(2, "返回结果"),

    /**
     * 来源于参数与结果的差集
     */
    REQUEST_MINUS_RETURN(3, "请求参数与返回结果的差集"),

    /**
     * 来源于参数与结果的并集
     */
    REQUEST_UNION_RETURN(4, "请求参数与返回结果的并集"),

    /**
     * 来源于参数与结果的交集
     */
    REQUEST_INTERSECT_RETURN(5, "请求参数与返回结果的交集"),

    /**
     * 自定义
     */
    CUSTOM(6, "自定义"),

    /**
     * 请求参数与返回结果的tuple
     */
    REQUEST_RETURN_WRAPPER(7, "请求参数与返回结果的wrapper");

    private int type;
    private String name;

}
