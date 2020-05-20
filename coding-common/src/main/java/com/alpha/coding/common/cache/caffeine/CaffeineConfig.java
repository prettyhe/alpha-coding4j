package com.alpha.coding.common.cache.caffeine;

import java.io.Serializable;
import java.util.function.BiFunction;

import org.springframework.context.ApplicationContext;

import com.github.benmanes.caffeine.cache.CacheLoader;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * CaffeineConfig
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class CaffeineConfig implements Serializable {

    /**
     * 默认从ApplicationContext中根据{loaderName}拿
     */
    public static final BiFunction<ApplicationContext, String, CacheLoader> CACHE_LOADER_FUNCTION =
            (ApplicationContext applicationContext, String name) ->
                    (CacheLoader) applicationContext.getBean(name);

    private String cacheName;
    private String loaderName;
    private BiFunction<ApplicationContext, String, CacheLoader> loaderFunction;
    private boolean allowNullValues = true;
    private String spec;

}
