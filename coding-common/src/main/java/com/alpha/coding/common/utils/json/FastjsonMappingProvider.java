package com.alpha.coding.common.utils.json;

import com.alibaba.fastjson.JSON;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

/**
 * FastjsonMappingProvider
 *
 * @version 1.0
 * Date: 2021/1/23
 */
public class FastjsonMappingProvider implements MappingProvider {

    @Override
    public <T> T map(Object source, Class<T> targetType, Configuration configuration) {
        return JSON.toJavaObject((JSON) source, targetType);
    }

    @Override
    public <T> T map(Object source, TypeRef<T> targetType, Configuration configuration) {
        return source == null ? null : ((JSON) source).toJavaObject(targetType.getType());
    }

}
