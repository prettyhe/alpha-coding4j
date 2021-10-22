package com.alpha.coding.common.message.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MessageSendStatus
 *
 * @version 1.0
 * Date: 2021/9/8
 */
@Getter
@AllArgsConstructor
public enum MessageSendStatus {

    WAIT_SEND(0, "待发送"),
    SUCCESS(1, "成功"),
    FAIL(2, "失败"),
    WAIT_RECEIPT(3, "待回执"),
    CANCEL(4, "取消"),
    ;

    private final int code;
    private final String desc;

}
