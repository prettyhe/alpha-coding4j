package com.alpha.coding.common.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.alpha.coding.bo.function.common.Functions;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpClientUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class HttpClientUtils {

    public static final int DEFAULT_CONN_TIMEOUT = 3000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 3000;
    public static final String DEFAULT_CHARSET_STR = StandardCharsets.UTF_8.displayName();
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String IGNORE_REQ = "ignoreReq";

    /**
     * Get the entity content of {@link HttpResponse} as a String, using the provided default character set if none is
     * found in the entity. If defaultCharset is null, the default "ISO-8859-1" is used.
     * <p>
     *
     * @param response 响应，非空
     * @param charset  字符集
     */
    public static String parseResponse(HttpResponse response, String charset) throws ParseException, IOException {
        return EntityUtils.toString(response.getEntity(), charset);
    }

    /**
     * 根据表单构建HttpPost,默认使用UTF-8字符集
     *
     * @param url    请求地址
     * @param params 请求参数
     */
    public static HttpPost formPost(String url, Map<String, String> params) throws UnsupportedEncodingException {
        final HttpPost post = new HttpPost(url);
        final List<NameValuePair> pairs = new ArrayList<>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            pairs.add(new BasicNameValuePair(key, params.get(key)));
        }
        post.setEntity(new UrlEncodedFormEntity(pairs, DEFAULT_CHARSET_STR));
        return post;
    }

    /**
     * http get
     *
     * @param uri         uri，若为https，默认信任所有
     * @param params      参数,注意参数值可能需要Encode
     * @param charset     字符集
     * @param connTimeout 连接超时，-1表示不设超时
     * @param soTimeout   socket超时，-1表示不设超时
     * @return 返回结果
     * @throws IOException IOException
     */
    public static String get(String uri, Map<String, String> params, String charset, int connTimeout, int soTimeout)
            throws IOException {
        return get(uri, params, charset, connTimeout, soTimeout, null);
    }

    /**
     * http get
     *
     * @param uri         uri，若为https，默认信任所有
     * @param params      参数,注意参数值可能需要Encode
     * @param charset     字符集
     * @param connTimeout 连接超时，-1表示不设超时
     * @param soTimeout   socket超时，-1表示不设超时
     * @param getConsumer get请求回调函数
     * @return 返回结果
     * @throws IOException IOException
     */
    public static String get(String uri, Map<String, String> params, String charset, int connTimeout, int soTimeout,
                             Consumer<HttpGet> getConsumer) throws IOException {
        String url = uri;
        if (params != null && params.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sb.deleteCharAt(sb.lastIndexOf("&"));
            url = uri + (uri.contains("?") ? "&" : "?") + sb.toString();
        }
        return get(url, charset, connTimeout, soTimeout, 0, null, getConsumer);
    }

    /**
     * http get 请求
     *
     * @param url         请求
     * @param charset     编码
     * @param connTimeout 连接超时
     * @param soTimeout   socket超时
     * @param retry       重试次数
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String get(String url, String charset, int connTimeout, int soTimeout, int retry)
            throws IOException {
        return get(url, charset, connTimeout, soTimeout, retry,
                httpGet -> httpGet.setHeader("Connection", "close"));
    }

    /**
     * http get 请求
     *
     * @param url         请求，若为https默认信任所有
     * @param charset     编码
     * @param connTimeout 连接超时
     * @param soTimeout   socket超时
     * @param retry       重试次数
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String get(String url, String charset, int connTimeout, int soTimeout, int retry,
                             Consumer<HttpGet> getConsumer) throws IOException {
        return get(url, charset, connTimeout, soTimeout, retry, null, getConsumer);
    }

    /**
     * http get 请求
     *
     * @param url            请求
     * @param charset        编码
     * @param connTimeout    连接超时
     * @param soTimeout      socket超时
     * @param retry          重试次数
     * @param clientSupplier CloseableHttpClient提供者
     * @param getConsumer    get请求回调函数
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String get(String url, String charset, int connTimeout, int soTimeout, int retry,
                             Supplier<CloseableHttpClient> clientSupplier, Consumer<HttpGet> getConsumer)
            throws IOException {
        final long startTime = System.nanoTime();
        String ret = null;
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = clientSupplier != null ? clientSupplier.get()
                    : new DefaultHttpClientSupplier().setRetry(retry).setUseHttps(url.startsWith("https://")).get();
            RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(soTimeout)
                    .setConnectTimeout(connTimeout)
                    .setConnectionRequestTimeout(connTimeout)
                    .build();
            HttpGet get = new HttpGet(url);
            get.setConfig(config);
            if (getConsumer != null) {
                getConsumer.accept(get);
            }
            response = httpClient.execute(get);
            ret = parseResponse(response, charset);
        } finally {
            close(httpClient, response);
            final long endTime = System.nanoTime();
            log.debug("http-get: connTo={},soTo={},retry={},url={},res={},elapsed={}",
                    connTimeout, soTimeout, retry, url, ret,
                    Functions.formatNanos.apply(endTime - startTime));
        }
        return ret;
    }

    private static void close(Closeable... closeableObj) {
        for (Closeable closeable : closeableObj) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception e) {
                // nothing
            }
        }
    }

    /**
     * http post form 请求
     *
     * @param url         请求
     * @param params      请求参数json串
     * @param charset     编码，默认为UTF-8
     * @param connTimeout 连接超时,-1表示不设超时
     * @param soTimeout   socket超时,-1表示不设超时
     * @param retry       重试次数
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postForm(String url, Map<String, String> params, String charset,
                                  int connTimeout, int soTimeout, int retry) throws IOException {
        return postParams(url, params, charset, connTimeout, soTimeout, retry, null);
    }

    /**
     * http post params 请求, Content-Type: application/x-www-form-urlencoded
     *
     * @param url         请求
     * @param params      请求参数json串
     * @param charset     编码，默认为UTF-8
     * @param connTimeout 连接超时,-1表示不设超时
     * @param soTimeout   socket超时,-1表示不设超时
     * @param retry       重试次数
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postParams(String url, Map<String, String> params, String charset,
                                    int connTimeout, int soTimeout, int retry) throws IOException {
        return postParams(url, params, charset, connTimeout, soTimeout, retry,
                post -> post.setHeader("Content-Type", "application/x-www-form-urlencoded"));
    }

    /**
     * http post params 请求, Content-Type: application/x-www-form-urlencoded
     *
     * @param url              请求
     * @param params           请求参数json串
     * @param charset          编码，默认为UTF-8
     * @param connTimeout      连接超时,-1表示不设超时
     * @param soTimeout        socket超时,-1表示不设超时
     * @param retry            重试次数
     * @param httpPostConsumer post回调
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postParams(final String url, final Map<String, String> params, final String charset,
                                    final int connTimeout, final int soTimeout, final int retry,
                                    final Consumer<HttpPost> httpPostConsumer) throws IOException {
        final List<BasicNameValuePair> pairs = new ArrayList<>();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs,
                charset == null ? DEFAULT_CHARSET_STR : charset);
        return post(url, charset, retry, null,
                builder -> builder.setConnectTimeout(connTimeout)
                        .setSocketTimeout(soTimeout)
                        .setConnectionRequestTimeout(connTimeout),
                post -> {
                    post.setHeader("Connection", "close");
                    post.setEntity(entity);
                    if (httpPostConsumer != null) {
                        httpPostConsumer.accept(post);
                    }
                },
                (logger, httpExecRes) -> logger
                        .debug("http-postParams: charset={},retry={},url={},req={},res={},elapsed={}ms",
                                charset, retry, url, ignoreReq() ? "" : params, httpExecRes.getRes(),
                                httpExecRes.getElapsedMillis()));
    }

    /**
     * http post body 请求, 适用于 Content-Type: application/json
     *
     * @param url         请求
     * @param reqJson     请求参数json串，放在body中
     * @param charset     编码，默认为UTF-8
     * @param connTimeout 连接超时,-1表示不设超时
     * @param soTimeout   socket超时,-1表示不设超时
     * @param retry       重试次数
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postBody(final String url, final String reqJson, final String charset,
                                  final int connTimeout, final int soTimeout, final int retry) throws IOException {
        return postBody(url, reqJson, charset, retry,
                builder -> builder.setConnectTimeout(connTimeout)
                        .setSocketTimeout(soTimeout)
                        .setConnectionRequestTimeout(connTimeout),
                null);
    }

    /**
     * http post body 请求, 适用于 Content-Type: application/json
     *
     * @param url         请求
     * @param reqJson     请求参数json串，放在body中
     * @param charset     编码，默认为UTF-8
     * @param connTimeout 连接超时,-1表示不设超时
     * @param soTimeout   socket超时,-1表示不设超时
     * @param retry       重试次数
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postBody(final String url, final String reqJson, final String charset,
                                  final int connTimeout, final int soTimeout, final int retry,
                                  final Consumer<HttpPost> httpPostConsumer) throws IOException {
        return postBody(url, reqJson, charset, retry,
                builder -> builder.setConnectTimeout(connTimeout)
                        .setSocketTimeout(soTimeout)
                        .setConnectionRequestTimeout(connTimeout),
                httpPostConsumer);
    }

    /**
     * 检查打印日志是否需要忽略请求参数
     */
    private static boolean ignoreReq() {
        if (MDC.getMDCAdapter() == null) {
            return false;
        }
        final String logReq = MDC.getMDCAdapter().get(IGNORE_REQ);
        return "true".equals(logReq);
    }

    /**
     * http post body, closable, Content-Type: application/json
     *
     * @param url                   url
     * @param reqJson               json串
     * @param charset               字符集
     * @param retry                 重试次数
     * @param requestConfigConsumer 配置回调
     * @param httpPostConsumer      post回调
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postBody(final String url, final String reqJson, final String charset, final int retry,
                                  final Consumer<RequestConfig.Builder> requestConfigConsumer,
                                  final Consumer<HttpPost> httpPostConsumer) throws IOException {
        return post(url, charset, retry, null,
                requestConfigConsumer,
                post -> {
                    post.setHeader("Connection", "close");
                    ContentType contentType = ContentType.create("application/json",
                            charset == null ? DEFAULT_CHARSET_STR : charset);
                    StringEntity entity = new StringEntity(reqJson, contentType);
                    post.setEntity(entity);
                    if (httpPostConsumer != null) {
                        httpPostConsumer.accept(post);
                    }
                },
                (logger, httpExecRes) -> logger
                        .debug("http-postBody: charset={},retry={},url={},req={},res={},elapsed={}ms",
                                charset, retry, url, ignoreReq() ? "" : reqJson, httpExecRes.getRes(),
                                httpExecRes.getElapsedMillis()));
    }

    /**
     * http post 请求
     *
     * @param url                   请求
     * @param charset               编码
     * @param retry                 重试次数
     * @param clientSupplier        CloseableHttpClient提供者
     * @param requestConfigConsumer 配置回调
     * @param postConsumer          post回调
     * @param finalLogConsumer      执行完log回调
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String post(String url, String charset, int retry,
                              Supplier<CloseableHttpClient> clientSupplier,
                              Consumer<RequestConfig.Builder> requestConfigConsumer,
                              Consumer<HttpPost> postConsumer,
                              BiConsumer<Logger, HttpExecRes> finalLogConsumer) throws IOException {
        final long startTime = System.nanoTime();
        String ret = null;
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = clientSupplier != null ? clientSupplier.get()
                    : new DefaultHttpClientSupplier()
                            .setRetry(retry)
                            .setUseHttps(url.startsWith("https://"))
                            .get();
            final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            if (requestConfigConsumer != null) {
                requestConfigConsumer.accept(requestConfigBuilder);
            }
            HttpPost post = new HttpPost(url);
            post.setConfig(requestConfigBuilder.build());
            if (postConsumer != null) {
                postConsumer.accept(post);
            }
            response = httpClient.execute(post);
            ret = parseResponse(response, charset == null ? DEFAULT_CHARSET_STR : charset);
        } finally {
            close(httpClient, response);
            final long endTime = System.nanoTime();
            if (finalLogConsumer != null) {
                finalLogConsumer.accept(log,
                        new HttpExecRes(TimeUnit.NANOSECONDS.toMillis(endTime - startTime), ret));
            }
        }
        return ret;
    }

    @Data
    @Accessors(chain = true)
    public static class HttpExecRes implements Serializable {
        private long elapsedMillis; // 耗时(ms)
        private String res; // 响应结果

        public HttpExecRes(long elapsedMillis, String res) {
            this.elapsedMillis = elapsedMillis;
            this.res = res;
        }
    }

}
