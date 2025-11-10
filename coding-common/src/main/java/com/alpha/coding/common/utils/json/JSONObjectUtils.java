package com.alpha.coding.common.utils.json;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

/**
 * JSONObjectUtils
 *
 * @version 1.0
 */
public class JSONObjectUtils {

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param path       相对于根的绝对目录,如a.b
     * @param pathSep    路径分隔符，如\\.（转义）
     */
    @Deprecated
    public static Object getValue(JSONObject jsonObject, String path, String pathSep) {
        return getValue(jsonObject, path, pathSep, Object.class);
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param path       相对于根的绝对目录,默认半角逗号分隔,如a.b
     */
    public static <T> T getValue(JSONObject jsonObject, String path, Type type) {
        return getValue(jsonObject, path.split("\\."), type);
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param path       相对于根的绝对目录,默认半角逗号分隔,如a.b
     */
    public static <T> T getValue(JSONObject jsonObject, String path, String sep, Type type) {
        return getValue(jsonObject, path.split(sep), type);
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param path       相对于根的绝对目录,默认半角逗号分隔,如a.b
     */
    public static <T> T getValue(JSONObject jsonObject, String path, TypeReference<T> typeReference) {
        return getValue(jsonObject, path.split("\\."), typeReference);
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param paths      相对于根的绝对目录序列,如[a,b]
     */
    @Deprecated
    public static Object getValue(JSONObject jsonObject, String[] paths) {
        return getValue(jsonObject, paths, Object.class);
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param paths      相对于根的绝对目录序列,如[a,b]
     */
    public static <T> T getValue(JSONObject jsonObject, String[] paths, Type type) {
        int i = 0;
        Object obj = jsonObject;
        while (i < paths.length - 1) {
            if (obj == null) {
                return null;
            }
            obj = JSON.toJSON(obj);
            if (obj instanceof JSONObject) {
                obj = ((JSONObject) obj).get(paths[i++]);
            } else {
                return null;
            }
        }
        if (obj == null) {
            return null;
        }
        obj = JSON.toJSON(obj);
        if (obj instanceof JSONObject) {
            return ((JSONObject) obj).getObject(paths[i], type);
        }
        return null;
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param paths      相对于根的绝对目录序列,如[a,b]
     */
    public static <T> T getValue(JSONObject jsonObject, String[] paths, TypeReference<T> typeReference) {
        int i = 0;
        Object obj = jsonObject;
        while (i < paths.length - 1) {
            if (obj == null) {
                return null;
            }
            obj = JSON.toJSON(obj);
            if (obj instanceof JSONObject) {
                obj = ((JSONObject) obj).get(paths[i++]);
            } else {
                return null;
            }
        }
        if (obj == null) {
            return null;
        }
        obj = JSON.toJSON(obj);
        if (obj instanceof JSONObject) {
            return ((JSONObject) obj).getObject(paths[i], typeReference);
        }
        return null;
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"c":1}
     * @param path       相对于根的绝对目录,默认半角逗号分隔,如a.b
     * @param value      值，如test，结果jsonObject变成{"a":{"b":"test"},"c":1}
     */
    public static boolean putValue(JSONObject jsonObject, String path, Object value) {
        return putValue(jsonObject, path.split("\\."), value);
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"c":1}
     * @param path       相对于根的绝对目录,默认半角逗号分隔,如a.b
     * @param sep        路径分隔符
     * @param value      值，如test，结果jsonObject变成{"a":{"b":"test"},"c":1}
     */
    public static boolean putValue(JSONObject jsonObject, String path, String sep, Object value) {
        return putValue(jsonObject, path.split(sep), value);
    }

    /**
     * 修改jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"c":1}
     * @param paths      相对于根的绝对目录序列,如[a,b]
     * @param value      值，如test，结果jsonObject变成{"a":{"b":"test"},"c":1}
     */
    public static boolean putValue(JSONObject jsonObject, String[] paths, final Object value) {
        return updateNode(jsonObject, paths, (json, key) -> json.put(key, value));
    }

    /**
     * 更新节点
     */
    private static boolean updateNode(JSONObject jsonObject, String[] paths, BiConsumer<JSONObject, String> action) {
        if (jsonObject == null) {
            return false;
        }
        int i = 0;
        Object obj = jsonObject;
        while (i < paths.length - 1) {
            String key = paths[i++];
            Object parent = obj;
            if (!(parent instanceof JSONObject)) {
                return false;
            }
            obj = ((JSONObject) parent).computeIfAbsent(key, k -> new JSONObject());
        }
        if (obj instanceof JSONObject) {
            action.accept((JSONObject) obj, paths[i]);
            return true;
        }
        return false;
    }

    /**
     * 删除jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param path       相对于根的绝对路径,默认半角逗号分隔,如a.b
     */
    public static boolean removeValue(JSONObject jsonObject, String path) {
        return updateNode(jsonObject, path.split("\\."), JSONObject::remove);
    }

    /**
     * 删除jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param paths      相对于根的绝对目录序列,如[a,b]
     */
    public static boolean removeValue(JSONObject jsonObject, String[] paths) {
        return updateNode(jsonObject, paths, JSONObject::remove);
    }

}
