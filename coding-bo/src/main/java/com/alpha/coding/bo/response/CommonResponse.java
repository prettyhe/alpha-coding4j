package com.alpha.coding.bo.response;

import java.io.Serializable;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;

import com.alpha.coding.bo.base.CodeMsgSupplier;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * CommonResponse
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class CommonResponse<T> implements CodeMsgSupplier, Predicate<Integer>, Serializable {

    private Integer code;
    private String msg;
    private T data;

    public static <T> CommonResponse<T> build(int code, String msg) {
        return new CommonResponse<T>().setCode(code).setMsg(msg);
    }

    public static <T> CommonResponse<T> build(int code, String msg, T t) {
        return new CommonResponse<T>().setData(t).setCode(code).setMsg(msg);
    }

    public static <T> CommonResponse<T> build(@NotNull CodeMsgSupplier codeMsgSupplier) {
        return build(codeMsgSupplier.codeSupplier().get(), codeMsgSupplier.msgSupplier().get());
    }

    public static <T> CommonResponse<T> build(@NotNull CodeMsgSupplier codeMsgSupplier, T t) {
        return new CommonResponse<T>().setData(t)
                .setCode(codeMsgSupplier.codeSupplier().get())
                .setMsg(codeMsgSupplier.msgSupplier().get());
    }

    @Override
    public Supplier<Integer> codeSupplier() {
        return () -> CommonResponse.this.getCode();
    }

    @Override
    public Supplier<String> msgSupplier() {
        return () -> CommonResponse.this.getMsg();
    }

    @Override
    public boolean test(Integer code) {
        return (code == null && this.code == null) || (code.intValue() == this.code.intValue());
    }
}
