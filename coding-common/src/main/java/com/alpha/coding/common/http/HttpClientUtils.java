package com.alpha.coding.common.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.alibaba.fastjson.JSON;
import com.alpha.coding.bo.function.common.Functions;
import com.alpha.coding.common.http.model.HttpConfig;
import com.alpha.coding.common.http.model.MultipartFileItem;
import com.alpha.coding.common.utils.StringUtils;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * ApacheHttpClient 工具类
 * 当启用系统配置`enableDefaultProxy=true`时，自动识别系统配置代理
 * <li>http.proxyHost</li>
 * <li>http.proxyPort</li>
 * <li>http.proxyUser</li>
 * <li>http.proxyPassword</li>
 * <li>https.proxyHost</li>
 * <li>https.proxyPort</li>
 * <li>https.proxyUser</li>
 * <li>https.proxyPassword</li>
 * <li>http.nonProxyHosts</li>
 * <li>socksProxyHost</li>
 * <li>socksProxyPort</li>
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
        if (params != null && !params.isEmpty()) {
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
     * http get 请求, 推荐使用
     *
     * @param uri        uri，若为https，默认信任所有
     * @param params     参数,注意参数值可能需要Encode
     * @param charset    字符集
     * @param httpConfig 请求配置(须非空)
     * @return 返回结果
     * @throws IOException IOException
     */
    public static String get(String uri, Map<String, String> params, String charset, HttpConfig httpConfig)
            throws IOException {
        return get(uri, params, charset, httpConfig, null);
    }

    /**
     * http get 请求, 推荐使用
     *
     * @param uri         uri，若为https，默认信任所有
     * @param params      参数,注意参数值可能需要Encode
     * @param charset     字符集
     * @param httpConfig  请求配置(须非空)
     * @param getConsumer get请求回调函数
     * @return 返回结果
     * @throws IOException IOException
     */
    public static String get(String uri, Map<String, String> params, String charset, HttpConfig httpConfig,
                             Consumer<HttpGet> getConsumer) throws IOException {
        String url = uri;
        if (params != null && !params.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sb.deleteCharAt(sb.lastIndexOf("&"));
            url = uri + (uri.contains("?") ? "&" : "?") + sb.toString();
        }
        return get(url, charset, httpConfig, null, getConsumer);
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
        return get(url, charset,
                HttpConfig.create().setConnTimeout(connTimeout).setSoTimeout(soTimeout).setRetry(retry),
                clientSupplier, getConsumer);
    }

    /**
     * http get 请求, 推荐使用
     *
     * @param url        请求
     * @param charset    编码
     * @param httpConfig 请求配置(须非空)
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String get(String url, String charset, HttpConfig httpConfig) throws IOException {
        return get(url, charset, httpConfig, null);
    }

    /**
     * http get 请求, 推荐使用
     *
     * @param url         请求
     * @param charset     编码
     * @param httpConfig  请求配置(须非空)
     * @param getConsumer get请求回调函数
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String get(String url, String charset, HttpConfig httpConfig, Consumer<HttpGet> getConsumer)
            throws IOException {
        return get(url, charset, httpConfig, null, getConsumer);
    }

    /**
     * http get 请求
     *
     * @param url            请求
     * @param charset        编码
     * @param httpConfig     请求配置(须非空)
     * @param clientSupplier CloseableHttpClient提供者
     * @param getConsumer    get请求回调函数
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String get(String url, String charset, HttpConfig httpConfig,
                             Supplier<CloseableHttpClient> clientSupplier, Consumer<HttpGet> getConsumer)
            throws IOException {
        final long startTime = System.nanoTime();
        final HttpGetContext httpGetContext = new HttpGetContext()
                .setUrl(url).setCharset(charset).setHttpConfig(httpConfig)
                .setClientSupplier(clientSupplier).setGetConsumer(getConsumer);
        String ret = null;
        try {
            doGet(httpGetContext);
            ret = parseResponse(httpGetContext.response, charset);
        } finally {
            close(httpGetContext.httpClient, httpGetContext.response);
            final long endTime = System.nanoTime();
            log.debug("http-get: connTo={},soTo={},retry={},url={},res={},elapsed={}",
                    httpConfig.getConnTimeout(), httpConfig.getSoTimeout(), httpConfig.getRetry(), url, ret,
                    Functions.formatNanos.apply(endTime - startTime));
        }
        return ret;
    }

    /**
     * http get 请求
     *
     * @param url            请求
     * @param charset        编码
     * @param httpConfig     请求配置(须非空)
     * @param clientSupplier CloseableHttpClient提供者
     * @param getConsumer    get请求回调函数
     * @throws IOException IOException
     */
    public static void get(String url, String charset, HttpConfig httpConfig,
                           Supplier<CloseableHttpClient> clientSupplier, Consumer<HttpGet> getConsumer,
                           Consumer<HttpResponse> httpResponseConsumer) throws IOException {
        final long startTime = System.nanoTime();
        final HttpGetContext httpGetContext = new HttpGetContext()
                .setUrl(url).setCharset(charset).setHttpConfig(httpConfig)
                .setClientSupplier(clientSupplier).setGetConsumer(getConsumer);
        try {
            doGet(httpGetContext);
            if (httpResponseConsumer != null) {
                httpResponseConsumer.accept(httpGetContext.response);
            }
        } finally {
            close(httpGetContext.httpClient, httpGetContext.response);
            final long endTime = System.nanoTime();
            log.debug("http-get: connTo={},soTo={},retry={},url={},res={},elapsed={}",
                    httpConfig.getConnTimeout(), httpConfig.getSoTimeout(), httpConfig.getRetry(), url, null,
                    Functions.formatNanos.apply(endTime - startTime));
        }
    }

    /**
     * 构建并执行post请求
     */
    private static void doGet(HttpGetContext context) throws IOException {
        context.httpClient = context.clientSupplier != null ? context.clientSupplier.get()
                : DefaultHttpClientSupplier.create().setHttpConfig(context.httpConfig)
                        .setUseHttps(context.url.startsWith("https://")).get();
        final RequestConfig.Builder configBuilder = RequestConfig.custom();
        configBuilder.setSocketTimeout(context.httpConfig.getSoTimeout());
        configBuilder.setConnectTimeout(context.httpConfig.getConnTimeout());
        configBuilder.setConnectionRequestTimeout(context.httpConfig.getConnTimeout());
        if (StringUtils.isNotBlank(context.httpConfig.getHttpProxyHost())
                && context.httpConfig.getHttpProxyPort() > 0) {
            configBuilder.setProxy(new HttpHost(context.httpConfig.getHttpProxyHost(),
                    context.httpConfig.getHttpProxyPort()));
        }
        if (context.requestConfigConsumer != null) {
            context.requestConfigConsumer.accept(configBuilder);
        }
        final RequestConfig requestConfig = configBuilder.build();
        final HttpGet httpGet = new HttpGet(context.url);
        httpGet.setConfig(requestConfig);
        if (context.getConsumer != null) {
            context.getConsumer.accept(httpGet);
        }
        context.response = context.httpClient.execute(httpGet);
    }

    @Data
    @Accessors(chain = true)
    private static class HttpGetContext {
        private CloseableHttpClient httpClient;
        private CloseableHttpResponse response;
        private String url;
        private String charset;
        private HttpConfig httpConfig;
        private Supplier<CloseableHttpClient> clientSupplier;
        private Consumer<RequestConfig.Builder> requestConfigConsumer;
        private Consumer<HttpGet> getConsumer;
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
     * @param params      请求参数
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
     * @param params      请求参数
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
     * http post params 请求, Content-Type: application/x-www-form-urlencoded, 推荐使用
     *
     * @param url        请求
     * @param params     请求参数
     * @param charset    编码，默认为UTF-8
     * @param httpConfig 请求配置（须非空）
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postParams(String url, Map<String, String> params, String charset,
                                    HttpConfig httpConfig) throws IOException {
        return postParams(url, params, charset, httpConfig,
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
        return postParams(url, params, charset,
                HttpConfig.create().setConnTimeout(connTimeout).setSoTimeout(soTimeout).setRetry(retry),
                httpPostConsumer);
    }

    /**
     * http post params 请求, Content-Type: application/x-www-form-urlencoded, 推荐使用
     *
     * @param url              请求
     * @param params           请求参数json串
     * @param charset          编码，默认为UTF-8
     * @param httpConfig       请求配置（须非空）
     * @param httpPostConsumer post回调
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postParams(final String url, final Map<String, String> params,
                                    final String charset, final HttpConfig httpConfig,
                                    final Consumer<HttpPost> httpPostConsumer) throws IOException {
        final List<BasicNameValuePair> pairs = new ArrayList<>();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs,
                charset == null ? DEFAULT_CHARSET_STR : charset);
        return post(url, charset, httpConfig, null, null,
                post -> {
                    post.setHeader("Connection", "close");
                    post.setEntity(entity);
                    if (httpPostConsumer != null) {
                        httpPostConsumer.accept(post);
                    }
                },
                (logger, httpExecRes) -> logger
                        .debug("http-postParams: charset={},retry={},url={},req={},res={},elapsed={}ms",
                                charset, httpConfig.getRetry(), url, ignoreReq() ? "" : params, httpExecRes.getRes(),
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
        return postBody(url, reqJson, charset,
                HttpConfig.create().setConnTimeout(connTimeout).setSoTimeout(soTimeout).setRetry(retry),
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
        return postBody(url, reqJson, charset,
                HttpConfig.create().setConnTimeout(connTimeout).setSoTimeout(soTimeout).setRetry(retry),
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
        return postBody(url, reqJson, charset, HttpConfig.create().setRetry(retry),
                requestConfigConsumer, httpPostConsumer);
    }

    /**
     * http post body, closable, Content-Type: application/json, 推荐使用
     *
     * @param url        url
     * @param reqJson    json串
     * @param charset    字符集
     * @param httpConfig 请求配置(须非空)
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postBody(final String url, final String reqJson, final String charset,
                                  final HttpConfig httpConfig) throws IOException {
        return postBody(url, reqJson, charset, httpConfig, null);
    }

    /**
     * http post body, closable, Content-Type: application/json, 推荐使用
     *
     * @param url              url
     * @param reqJson          json串
     * @param charset          字符集
     * @param httpConfig       请求配置(须非空)
     * @param httpPostConsumer post回调
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postBody(final String url, final String reqJson, final String charset,
                                  final HttpConfig httpConfig,
                                  final Consumer<HttpPost> httpPostConsumer) throws IOException {
        return postBody(url, reqJson, charset, httpConfig, null, httpPostConsumer);
    }

    /**
     * http post body, closable, Content-Type: application/json
     *
     * @param url              url
     * @param reqJson          json串
     * @param charset          字符集
     * @param httpConfig       请求配置(须非空)
     * @param httpPostConsumer post回调
     * @return 成功返回结果
     * @throws IOException IOException
     */
    public static String postBody(final String url, final String reqJson, final String charset,
                                  final HttpConfig httpConfig,
                                  final Consumer<RequestConfig.Builder> requestConfigConsumer,
                                  final Consumer<HttpPost> httpPostConsumer) throws IOException {
        return post(url, charset, httpConfig, null, requestConfigConsumer,
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
                                charset, httpConfig.getRetry(), url, ignoreReq() ? "" : reqJson, httpExecRes.getRes(),
                                httpExecRes.getElapsedMillis()));
    }

    /**
     * http post Multipart 请求
     *
     * @param url          请求
     * @param charset      编码
     * @param parameterMap 表单参数
     * @param fileMap      文件参数
     * @param httpConfig   请求配置(须非空)
     * @param postConsumer post回调
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String postMultipartMap(String url, String charset, Map<String, String> parameterMap,
                                          Map<String, MultipartFileItem> fileMap, HttpConfig httpConfig,
                                          Consumer<HttpPost> postConsumer) throws IOException {
        final Map<String, String[]> parameterMultiMap = new LinkedHashMap<>();
        if (parameterMap != null) {
            parameterMap.forEach((k, v) -> parameterMultiMap.put(k, new String[] {v}));
        }
        final Map<String, MultipartFileItem[]> fileMultiMap = new LinkedHashMap<>();
        if (fileMap != null) {
            fileMap.forEach((k, v) -> fileMultiMap.put(k, new MultipartFileItem[] {v}));
        }
        return postMultipart(url, charset, parameterMultiMap, fileMultiMap, httpConfig, null, null, postConsumer,
                (logger, httpExecRes) -> logger
                        .debug("http-postMultipart: charset={},retry={},url={},parameterMap={},res={},elapsed={}ms",
                                charset, httpConfig.getRetry(), url, ignoreReq() ? "" : JSON.toJSONString(parameterMap),
                                httpExecRes.getRes(), httpExecRes.getElapsedMillis()));
    }

    /**
     * http post Multipart 请求
     *
     * @param url          请求
     * @param charset      编码
     * @param parameterMap 表单参数
     * @param fileMap      文件参数
     * @param httpConfig   请求配置(须非空)
     * @param postConsumer post回调
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String postMultipart(String url, String charset, Map<String, String[]> parameterMap,
                                       Map<String, MultipartFileItem[]> fileMap, HttpConfig httpConfig,
                                       Consumer<HttpPost> postConsumer) throws IOException {
        return postMultipart(url, charset, parameterMap, fileMap, httpConfig, null, null, postConsumer,
                (logger, httpExecRes) -> logger
                        .debug("http-postMultipart: charset={},retry={},url={},parameterMap={},res={},elapsed={}ms",
                                charset, httpConfig.getRetry(), url, ignoreReq() ? "" : JSON.toJSONString(parameterMap),
                                httpExecRes.getRes(), httpExecRes.getElapsedMillis()));
    }

    /**
     * http post Multipart 请求
     *
     * @param url                   请求
     * @param charset               编码
     * @param parameterMap          表单参数
     * @param fileMap               文件参数
     * @param httpConfig            请求配置(须非空)
     * @param clientSupplier        CloseableHttpClient提供者
     * @param requestConfigConsumer 配置回调
     * @param postConsumer          post回调
     * @param finalLogConsumer      执行完log回调
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String postMultipart(String url, String charset, Map<String, String[]> parameterMap,
                                       Map<String, MultipartFileItem[]> fileMap, HttpConfig httpConfig,
                                       Supplier<CloseableHttpClient> clientSupplier,
                                       Consumer<RequestConfig.Builder> requestConfigConsumer,
                                       Consumer<HttpPost> postConsumer,
                                       BiConsumer<Logger, HttpExecRes> finalLogConsumer) throws IOException {
        return post(url, charset, httpConfig, clientSupplier, requestConfigConsumer,
                post -> {
                    post.setHeader("Content-Type", ContentType.MULTIPART_FORM_DATA.getMimeType());
                    final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    final Charset cs = Optional.ofNullable(charset).map(Charset::forName)
                            .orElse(StandardCharsets.UTF_8);
                    builder.setCharset(cs);
                    final ContentType itemCType = ContentType.create("text/plain", charset);
                    if (parameterMap != null) {
                        parameterMap.forEach((k, vs) -> {
                            for (String v : vs) {
                                builder.addTextBody(k, v, itemCType);
                            }
                        });
                    }
                    if (fileMap != null) {
                        fileMap.forEach((k, vs) -> {
                            for (MultipartFileItem v : vs) {
                                String filename = v.getFilename() == null ? "" : v.getFilename();
                                try {
                                    filename = URLEncoder.encode(filename, cs.name());
                                } catch (UnsupportedEncodingException e) {
                                    log.warn("encode filename fail, filename={}", filename, e);
                                }
                                builder.addBinaryBody(k, v.getInputStream(),
                                        Optional.ofNullable(v.getContentType()).map(ContentType::parse)
                                                .orElse(ContentType.APPLICATION_OCTET_STREAM), filename);
                            }
                        });
                    }
                    post.setEntity(builder.build());
                    if (postConsumer != null) {
                        postConsumer.accept(post);
                    }
                }, finalLogConsumer);
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
        return post(url, charset, HttpConfig.create().setRetry(retry), clientSupplier,
                requestConfigConsumer, postConsumer, finalLogConsumer);
    }

    /**
     * http post 请求
     *
     * @param url                   请求
     * @param charset               编码
     * @param httpConfig            请求配置(须非空)
     * @param clientSupplier        CloseableHttpClient提供者
     * @param requestConfigConsumer 配置回调
     * @param postConsumer          post回调
     * @param finalLogConsumer      执行完log回调
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String post(String url, String charset, HttpConfig httpConfig,
                              Supplier<CloseableHttpClient> clientSupplier,
                              Consumer<RequestConfig.Builder> requestConfigConsumer,
                              Consumer<HttpPost> postConsumer,
                              BiConsumer<Logger, HttpExecRes> finalLogConsumer) throws IOException {
        final long startTime = System.nanoTime();
        final HttpPostContext httpPostContext = new HttpPostContext()
                .setUrl(url).setCharset(charset).setHttpConfig(httpConfig)
                .setClientSupplier(clientSupplier).setRequestConfigConsumer(requestConfigConsumer)
                .setPostConsumer(postConsumer);
        String ret = null;
        try {
            doPost(httpPostContext);
            ret = parseResponse(httpPostContext.response, charset == null ? DEFAULT_CHARSET_STR : charset);
        } finally {
            close(httpPostContext.httpClient, httpPostContext.response);
            final long endTime = System.nanoTime();
            if (finalLogConsumer != null) {
                finalLogConsumer.accept(log,
                        new HttpExecRes(TimeUnit.NANOSECONDS.toMillis(endTime - startTime), ret));
            }
        }
        return ret;
    }

    /**
     * http post 请求
     *
     * @param url                   请求
     * @param charset               编码
     * @param httpConfig            请求配置(须非空)
     * @param clientSupplier        CloseableHttpClient提供者
     * @param requestConfigConsumer 配置回调
     * @param postConsumer          post回调
     * @param httpResponseConsumer  响应体消费
     * @param finalLogConsumer      执行完log回调
     * @throws IOException IOException
     */
    public static void post(String url, String charset, HttpConfig httpConfig,
                            Supplier<CloseableHttpClient> clientSupplier,
                            Consumer<RequestConfig.Builder> requestConfigConsumer,
                            Consumer<HttpPost> postConsumer,
                            Consumer<HttpResponse> httpResponseConsumer,
                            BiConsumer<Logger, HttpExecRes> finalLogConsumer) throws IOException {
        final long startTime = System.nanoTime();
        final HttpPostContext httpPostContext = new HttpPostContext()
                .setUrl(url).setCharset(charset).setHttpConfig(httpConfig)
                .setClientSupplier(clientSupplier).setRequestConfigConsumer(requestConfigConsumer)
                .setPostConsumer(postConsumer);
        try {
            doPost(httpPostContext);
            if (httpResponseConsumer != null) {
                httpResponseConsumer.accept(httpPostContext.response);
            }
        } finally {
            close(httpPostContext.httpClient, httpPostContext.response);
            final long endTime = System.nanoTime();
            if (finalLogConsumer != null) {
                finalLogConsumer.accept(log,
                        new HttpExecRes(TimeUnit.NANOSECONDS.toMillis(endTime - startTime), null));
            }
        }
    }

    /**
     * 构建并执行post请求
     */
    private static void doPost(HttpPostContext context) throws IOException {
        context.httpClient = context.clientSupplier != null ? context.clientSupplier.get()
                : DefaultHttpClientSupplier.create().setHttpConfig(context.httpConfig)
                        .setUseHttps(context.url.startsWith("https://")).get();
        final RequestConfig.Builder configBuilder = RequestConfig.custom();
        configBuilder.setSocketTimeout(context.httpConfig.getSoTimeout());
        configBuilder.setConnectTimeout(context.httpConfig.getConnTimeout());
        configBuilder.setConnectionRequestTimeout(context.httpConfig.getConnTimeout());
        if (StringUtils.isNotBlank(context.httpConfig.getHttpProxyHost())
                && context.httpConfig.getHttpProxyPort() > 0) {
            configBuilder.setProxy(new HttpHost(context.httpConfig.getHttpProxyHost(),
                    context.httpConfig.getHttpProxyPort()));
        }
        if (context.requestConfigConsumer != null) {
            context.requestConfigConsumer.accept(configBuilder);
        }
        final HttpPost httpPost = new HttpPost(context.url);
        httpPost.setConfig(configBuilder.build());
        if (context.postConsumer != null) {
            context.postConsumer.accept(httpPost);
        }
        context.response = context.httpClient.execute(httpPost);
    }

    @Data
    @Accessors(chain = true)
    private static class HttpPostContext {
        private CloseableHttpClient httpClient;
        private CloseableHttpResponse response;
        private String url;
        private String charset;
        private HttpConfig httpConfig;
        private Supplier<CloseableHttpClient> clientSupplier;
        private Consumer<RequestConfig.Builder> requestConfigConsumer;
        private Consumer<HttpPost> postConsumer;
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

    /**
     * http post 单文件上传 请求
     *
     * @param url                   请求
     * @param charset               编码
     * @param inputStream           上传文件流
     * @param httpConfig            请求配置(须非空)
     * @param clientSupplier        CloseableHttpClient提供者
     * @param requestConfigConsumer 配置回调
     * @param postConsumer          post回调
     * @param finalLogConsumer      执行完log回调
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String upload(String url, String charset, InputStream inputStream, HttpConfig httpConfig,
                                Supplier<CloseableHttpClient> clientSupplier,
                                Consumer<RequestConfig.Builder> requestConfigConsumer,
                                Consumer<HttpPost> postConsumer,
                                BiConsumer<Logger, HttpExecRes> finalLogConsumer) throws IOException {
        return post(url, charset, httpConfig, clientSupplier, requestConfigConsumer,
                post -> {
                    post.setHeader("Content-Type", ContentType.APPLICATION_OCTET_STREAM.getMimeType());
                    post.setEntity(new InputStreamEntity(inputStream));
                    if (postConsumer != null) {
                        postConsumer.accept(post);
                    }
                }, finalLogConsumer);
    }

    /**
     * http post 单文件上传 请求
     *
     * @param url          请求
     * @param charset      编码
     * @param inputStream  上传文件流
     * @param httpConfig   请求配置(须非空)
     * @param postConsumer post回调
     * @return 成功返回结果，失败返回null
     * @throws IOException IOException
     */
    public static String upload(String url, String charset, InputStream inputStream, HttpConfig httpConfig,
                                Consumer<HttpPost> postConsumer) throws IOException {
        return upload(url, charset, inputStream, httpConfig, null, null, postConsumer,
                (logger, httpExecRes) -> logger
                        .debug("http-upload: charset={},retry={},url={},res={},elapsed={}ms",
                                charset, httpConfig.getRetry(), url, httpExecRes.getRes(),
                                httpExecRes.getElapsedMillis()));
    }

}
