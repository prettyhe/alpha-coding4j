package com.alpha.coding.common.utils.json;

import java.io.IOException;
import java.io.InputStream;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alpha.coding.common.utils.IOUtils;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;

/**
 * FastjsonJsonProvider
 *
 * @version 1.0
 * Date: 2021/1/23
 */
public class FastjsonJsonProvider extends AbstractJsonProvider {

    @Override
    public Object parse(String json) throws InvalidJsonException {
        try {
            return JSON.parse(json);
        } catch (JSONException e) {
            return json;
        } catch (Exception e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public Object parse(InputStream inputStream, String charset) throws InvalidJsonException {
        try {
            return parse(new String(IOUtils.readInputStream(inputStream), charset));
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    @Override
    public String toJson(Object o) {
        return JSON.toJSONString(o);
    }

    @Override
    public Object createArray() {
        return new JSONArray();
    }

    @Override
    public Object createMap() {
        return new JSONObject();
    }

}
