package com.alpha.coding.bo.enums;

import java.util.function.Predicate;
import java.util.function.Supplier;

import com.alpha.coding.bo.base.CodeMsgSupplier;
import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CommResCode
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Getter
@AllArgsConstructor
public enum CommResCode implements EnumWithCodeSupplier, CodeMsgSupplier, Predicate<Integer> {

    SUCCESS(200, "success", "success"),
    PARAMS_ERROR(1001, "参数错误", "Parameter Error"),
    SYSTEM_ERROR(1002, "服务内部错误", "System Error"),
    BIZ_FAIL(1003, "业务失败", "Business Error"),

    OK(0, "OK", "OK"),
    ERR(-1, "Error", "Error"),
    PARAM_ERR(-9, "Parameter Error", "Parameter Error"),
    CUSTOM_ERR(-99, "", "");

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
}
