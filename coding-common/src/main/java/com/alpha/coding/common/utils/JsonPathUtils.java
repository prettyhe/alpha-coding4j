package com.alpha.coding.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alpha.coding.common.utils.json.FastjsonJsonProvider;
import com.alpha.coding.common.utils.json.FastjsonMappingProvider;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;

/**
 * JsonPathUtils
 *
 * @version 1.0
 * Date: 2022/3/23
 */
public class JsonPathUtils {

    /**
     * JsonPath缓存
     */
    private static final ConcurrentMap<String, JsonPath> JSON_PATH_MAP = new ConcurrentHashMap<>(64);
    /**
     * JsonPath使用的配置
     *
     * @see Configuration
     * @see <a href="https://github.com/json-path/JsonPath">https://github.com/json-path/JsonPath</a>
     */
    private static final Configuration JSON_CONFIGURATION = new Configuration.ConfigurationBuilder()
            .jsonProvider(new FastjsonJsonProvider())
            .mappingProvider(new FastjsonMappingProvider())
            .options(Configuration.defaultConfiguration().getOptions())
            .evaluationListener(Configuration.defaultConfiguration().getEvaluationListeners())
            .build();

    public static JsonPath remove(String path) {
        return JSON_PATH_MAP.remove(path);
    }

    public static void clean() {
        JSON_PATH_MAP.clear();
    }

    public static Object read(String json, String path) {
        final JsonPath jsonPath = JSON_PATH_MAP.computeIfAbsent(path, JsonPath::compile);
        DocumentContext context = JsonPath.using(JSON_CONFIGURATION).parse(json);
        return context.read(jsonPath);
    }

    public static <T> T read(String json, String path, Class<T> type) {
        final JsonPath jsonPath = JSON_PATH_MAP.computeIfAbsent(path, JsonPath::compile);
        DocumentContext context = JsonPath.using(JSON_CONFIGURATION).parse(json);
        return context.read(jsonPath, type);
    }

    public static <T> T read(String json, String path, TypeRef<T> type) {
        final JsonPath jsonPath = JSON_PATH_MAP.computeIfAbsent(path, JsonPath::compile);
        DocumentContext context = JsonPath.using(JSON_CONFIGURATION).parse(json);
        return context.read(jsonPath, type);
    }

}
