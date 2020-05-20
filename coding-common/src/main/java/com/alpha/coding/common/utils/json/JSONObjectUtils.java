package com.alpha.coding.common.utils.json;

import java.util.function.BiConsumer;

import com.alibaba.fastjson.JSONObject;

/**
 * JSONObjectUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class JSONObjectUtils {

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param path       相对于根的绝对目录,如a.b
     * @param pathSep    路径分隔符，如\\.（转义）
     */
    public static Object getValue(JSONObject jsonObject, String path, String pathSep) {
        String[] paths = path.split(pathSep);
        return getValue(jsonObject, paths);
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param paths      相对于根的绝对目录序列,如[a,b]
     */
    public static Object getValue(JSONObject jsonObject, String[] paths) {
        int i = 0;
        JSONObject obj = jsonObject;
        while (i < paths.length - 1) {
            if (obj == null) {
                return null;
            }
            obj = obj.getJSONObject(paths[i++]);
        }
        return obj == null ? null : obj.get(paths[i]);
    }

    /**
     * 获取jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"c":1}
     * @param path       相对于根的绝对目录,如a.b
     * @param pathSep    路径分隔符，如\\.（转义）
     * @param value      值，如test，结果jsonObject变成{"a":{"b":"test"},"c":1}
     */
    public static void putValue(JSONObject jsonObject, String path, String pathSep, Object value) {
        String[] paths = path.split(pathSep);
        putValue(jsonObject, paths, value);
    }

    /**
     * 修改jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"c":1}
     * @param paths      相对于根的绝对目录序列,如[a,b]
     * @param value      值，如test，结果jsonObject变成{"a":{"b":"test"},"c":1}
     */
    public static void putValue(JSONObject jsonObject, String[] paths, Object value) {
        updateNode(jsonObject, paths, (j, k) -> j.put(k, value));
    }

    private static JSONObject getJSONObjectExceptionToNull(JSONObject parent, String key) {
        final Object obj = parent.get(key);
        if (obj == null) {
            return null;
        }
        try {
            return parent.getJSONObject(key);
        } catch (Exception e) {
            return null;
        }
    }

    private static void updateNode(JSONObject jsonObject, String[] paths, BiConsumer<JSONObject, String> action) {
        int i = 0;
        JSONObject json = jsonObject;
        while (i < paths.length - 1) {
            String key = paths[i++];
            JSONObject obj = getJSONObjectExceptionToNull(json, key);
            if (obj == null) {
                obj = new JSONObject();
            }
            json.put(key, obj);
            json = obj;
        }
        action.accept(json, paths[i]);
    }

    /**
     * 删除jsonObject中路径为path的值
     *
     * @param jsonObject jsonObject,如{"a":{"b":1}}
     * @param paths      相对于根的绝对目录序列,如[a,b]
     */
    public static void removeValue(JSONObject jsonObject, String[] paths) {
        updateNode(jsonObject, paths, (j, k) -> j.remove(k));
    }

}
