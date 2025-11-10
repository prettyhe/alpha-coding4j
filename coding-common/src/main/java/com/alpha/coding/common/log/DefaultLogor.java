package com.alpha.coding.common.log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alpha.coding.common.utils.FieldUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * DefaultLogor
 * <p>
 * 优先取字段"baseResponse"下的"code"字段作为响应码，如果没有"baseResponse"字段，则直接取"code"字段
 *
 * @author js on 2017年8月8日
 * @version 1.0
 */
@Slf4j
public class DefaultLogor extends AbstractLogor {

    private static final ConcurrentMap<Class<?>, List<Field>> CODE_FIELD_MAP = new ConcurrentHashMap<>();

    @Override
    protected String getResponseCode(Class<?> responseClazz, Object response) throws Exception {
        if (response == null) {
            return "";
        }
        final List<Field> codeFieldPaths = CODE_FIELD_MAP.computeIfAbsent(responseClazz, k -> {
            Field[] fields = k.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                try {
                    if ("baseResponse".equals(field.getName())) {
                        if (!FieldUtils.isNeitherPrimitiveNorCollectionNorArray(field)) {
                            continue;
                        }
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        Field[] subFields = field.getType().getDeclaredFields();
                        for (Field subField : subFields) {
                            if ("code".equals(subField.getName())) {
                                if (Modifier.isStatic(subField.getModifiers())) {
                                    continue;
                                }
                                if (!subField.isAccessible()) {
                                    subField.setAccessible(true);
                                }
                                return Arrays.asList(field, subField);
                            }
                        }
                    } else if ("code".equals(field.getName())) {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        return Collections.singletonList(field);
                    }
                } catch (Exception e) {
                    log.warn("resolve code field fail for {} fail", k.getName(), e);
                }
            }
            return Collections.emptyList();
        });
        if (codeFieldPaths.isEmpty()) {
            return "";
        }
        Object target = response;
        for (Field field : codeFieldPaths) {
            target = field.get(target);
            if (target == null) {
                return "";
            }
        }
        return String.valueOf(target);
    }

}
