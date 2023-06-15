package com.alpha.coding.common.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * HttpFluentUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class HttpFluentUtils {

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset ASCII = Charset.forName("US-ASCII");
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    public static Request param(Request request, Map<String, String> params, Charset charset) {
        Form form = Form.form();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            form.add(entry.getKey(), entry.getValue());
        }
        return request.bodyForm(form.build(), charset == null ? ISO_8859_1 : charset);
    }

    public static String get(String uri) throws IOException {
        return Request.Get(uri).execute().returnContent().asString();
    }

    public static String get(String uri, Map<String, String> params, Charset charset) throws IOException {
        return param(Request.Get(uri), params, charset).execute().returnContent().asString();
    }

    public static String get(String uri, Map<String, String> params, Charset charset, int connectTimeout,
                             int socketTimeout) throws IOException {
        return param(Request.Get(uri), params, charset).connectTimeout(connectTimeout).socketTimeout(socketTimeout)
                .execute().returnContent().asString();
    }

    public static String post(String uri, Map<String, String> params, Charset charset) throws IOException {
        return param(Request.Post(uri), params, charset).execute().returnContent().asString();
    }

    public static String post(String uri, Map<String, String> params, Charset charset, int connectTimeout,
                              int socketTimeout) throws IOException {
        return param(Request.Post(uri), params, charset).connectTimeout(connectTimeout).socketTimeout(socketTimeout)
                .execute().returnContent().asString();
    }

    public static String postJson(String url, String jsonStr) throws IOException {
        return Request.Post(url).bodyString(jsonStr, ContentType.APPLICATION_JSON).execute().returnContent().asString();
    }

    public static String postObjectAsJson(String url, Object obj) throws IOException {
        return postJson(url, JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect));
    }

    public static String postJsonWithCookie(String url, String jsonStr, Cookie... cookies) throws IOException {
        CookieStore cookieStore = new BasicCookieStore();
        for (Cookie cookie : cookies) {
            cookieStore.addCookie(cookie);
        }
        Executor executor = Executor.newInstance();
        executor.cookieStore(cookieStore);
        return executor.execute(Request.Post(url).bodyString(jsonStr, ContentType.APPLICATION_JSON)).returnContent()
                .asString();
    }

    public static String postObjectAsJsonWithCookie(String url, Object obj, Cookie... cookies) throws IOException {
        return postJsonWithCookie(url, JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect),
                cookies);
    }

}
