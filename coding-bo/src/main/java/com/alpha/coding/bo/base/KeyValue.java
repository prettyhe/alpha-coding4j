package com.alpha.coding.bo.base;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * KeyValue
 *
 * @version 1.0
 * Date: 2021/12/9
 */
@Data
@Accessors(chain = true)
public class KeyValue implements Serializable {

    private String key;
    private Object value;
    private String type; // 类型：text-文本,number-数值,json-json,image-图片地址
    private Object valueExt; // 扩展值

    public static KeyValue of(String key, Object value, String type) {
        return new KeyValue().setKey(key).setValue(value).setType(type);
    }

    public static KeyValue ofText(String key, Object value) {
        return new KeyValue().setKey(key).setValue(value).setType("text");
    }

    public static KeyValue ofText(String key, Object value, Object valueExt) {
        return new KeyValue().setKey(key).setValue(value).setValueExt(valueExt).setType("text");
    }
}
