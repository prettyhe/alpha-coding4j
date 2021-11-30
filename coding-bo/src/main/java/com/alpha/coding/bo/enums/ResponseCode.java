package com.alpha.coding.bo.enums;

import java.util.function.Predicate;
import java.util.function.Supplier;

import com.alpha.coding.bo.base.CodeMsgSupplier;
import com.alpha.coding.bo.enums.util.CodeSupplyEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ResponseCode
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Getter
@AllArgsConstructor
public enum ResponseCode implements CodeSupplyEnum<ResponseCode>, CodeMsgSupplier, Predicate<Integer> {

    OK(0, "OK", "OK"),
    ERROR(700, "错误", "ERROR"),
    ERROR_CUSTOM(701, "", ""),
    ERROR_PARAM(702, "参数错误", "Parameter Error"),
    TIMEOUT(720, "请求超时", "Timeout"),
    ABANDON(731, "中断", "Abandon"),
    HANG(732, "挂起", "Hang Up"),
    NOT_LOGIN(801, "未登录", "Not Login"),
    AUTHENTICATION_FAIL(802, "无权限", "Authentication Fail");

    private final int code;
    private final String msg;
    private final String msgEn;

    @Override
    public Supplier codeSupply() {
        return this::getCode;
    }

    @Override
    public Supplier<Integer> codeSupplier() {
        return this::getCode;
    }

    @Override
    public Supplier<String> msgSupplier() {
        return this::getMsg;
    }

    @Override
    public boolean test(Integer code) {
        return code != null && this.code == code;
    }

    public static ResponseCode valueOf(int code) {
        return CodeSupplyEnum.valueOf(code);
    }

    public static String getDescByCode(int code) {
        return CodeSupplyEnum.getDescByCode(code);
    }
}
