package com.alpha.coding.bo.response;

import java.io.Serializable;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;

import com.alpha.coding.bo.base.CodeMsgSupplier;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * CommonPageResponse
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class CommonPageResponse<T> implements CodeMsgSupplier, Predicate<Integer>, Serializable {

    private Integer code;
    private String msg;
    private PageResData<T> data;

    public static <T> CommonPageResponse<T> build(int code, String msg) {
        return new CommonPageResponse<T>().setCode(code).setMsg(msg);
    }

    public static <T> CommonPageResponse<T> build(int code, String msg, PageResData<T> data) {
        return new CommonPageResponse<T>().setData(data).setCode(code).setMsg(msg);
    }

    public static <T> CommonPageResponse<T> build(@NotNull CodeMsgSupplier codeMsgSupplier) {
        return build(codeMsgSupplier.codeSupplier().get(), codeMsgSupplier.msgSupplier().get());
    }

    public static <T> CommonPageResponse<T> build(@NotNull CodeMsgSupplier codeMsgSupplier, PageResData<T> data) {
        return new CommonPageResponse<T>().setData(data)
                .setCode(codeMsgSupplier.codeSupplier().get())
                .setMsg(codeMsgSupplier.msgSupplier().get());
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
        return (code == null && this.code == null)
                || (code != null && this.code != null && code.intValue() == this.code.intValue());
    }
}
