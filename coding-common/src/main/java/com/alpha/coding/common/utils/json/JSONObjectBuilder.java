package com.alpha.coding.common.utils.json;

import com.alibaba.fastjson.JSONObject;

/**
 * JSONObjectBuilder
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class JSONObjectBuilder {

    private JSONObject jsonObject;

    public static JSONObjectBuilder builder() {
        return new JSONObjectBuilder(new JSONObject());
    }

    public static JSONObjectBuilder builder(JSONObject jsonObject) {
        return new JSONObjectBuilder(jsonObject);
    }

    public JSONObjectBuilder(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JSONObjectBuilder put(String key, Object value) {
        this.jsonObject.put(key, value);
        return this;
    }

    public JSONObject build() {
        return this.jsonObject;
    }
}
