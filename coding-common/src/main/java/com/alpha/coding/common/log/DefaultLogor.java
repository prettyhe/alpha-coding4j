/**
 * Copyright
 */
package com.alpha.coding.common.log;

import java.lang.reflect.Field;

/**
 * DefaultLogor
 * <p>
 * 优先取字段"baseResponse"下的"code"字段作为响应码，如果没有"baseResponse"字段，则直接取"code"字段
 * </p>
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class DefaultLogor extends AbstractLogor {

    @Override
    protected String getResponseCode(Class<?> responseClazz, Object response) throws Exception {
        Field[] fields = responseClazz.getDeclaredFields();
        Field codeField = null;
        for (Field field : fields) {
            if ("baseResponse".equalsIgnoreCase(field.getName())) {
                field.setAccessible(true);
                Object baseResponse = field.get(response);
                Class baseResponseClazz = baseResponse.getClass();
                Field[] innerFields = baseResponseClazz.getDeclaredFields();
                for (Field innerField : innerFields) {
                    if ("code".equalsIgnoreCase(innerField.getName())) {
                        innerField.setAccessible(true);
                        Object code = innerField.get(baseResponse);
                        return String.valueOf(code);
                    }
                }
            } else if ("code".equalsIgnoreCase(field.getName())) {
                codeField = field;
            }
        }
        if (codeField != null) {
            codeField.setAccessible(true);
            Object code = codeField.get(response);
            return String.valueOf(code);
        }
        return "";
    }

}
