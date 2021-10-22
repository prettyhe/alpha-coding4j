package com.alpha.coding.common.message.exception;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MessageException
 *
 * @version 1.0
 * Date: 2021/9/9
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageException extends RuntimeException implements Serializable {

    private String code;

    public MessageException() {
    }

    public MessageException(String message) {
        super(message);
    }

    public MessageException(String code, String message) {
        super(message);
        this.code = code;
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public MessageException(Throwable cause) {
        super(cause);
    }

    public MessageException(String message, Throwable cause, boolean enableSuppression,
                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
