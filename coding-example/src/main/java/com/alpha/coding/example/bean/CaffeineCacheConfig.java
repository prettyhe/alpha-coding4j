package com.alpha.coding.example.bean;

import com.alpha.coding.common.bean.define.BeanDefine;
import com.alpha.coding.common.bean.define.DefineType;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * CaffeineCacheConfig
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Data
@Accessors(chain = true)
@BeanDefine(type = DefineType.YAML, name = "CaffeineCacheConfig", src = "caffeine.yml")
public class CaffeineCacheConfig {

    private String cacheName;
    private String loaderName;
    private boolean allowNullValues = true;
    private String spec;

}
