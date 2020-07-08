package com.alpha.coding.common.http;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * CachedHttpComponentsClientHttpRequestFactory
 *
 * @version 1.0
 * Date: 2020/6/9
 */
@Data
@Accessors(chain = true)
public class CachedHttpComponentsClientHttpRequestFactory implements ClientHttpRequestFactory, DisposableBean {

    private final ConcurrentMap<String, HttpComponentsClientHttpRequestFactory> CACHE = new ConcurrentHashMap<>();

    private HttpClientConnectionManager httpClientConnectionManager;
    private RequestConfig requestConfig;

    @Override
    public void destroy() throws Exception {
        for (Map.Entry<String, HttpComponentsClientHttpRequestFactory> entry : CACHE.entrySet()) {
            entry.getValue().destroy();
        }
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        final URL url = uri.toURL();
        String key = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "");
        return CACHE.computeIfAbsent(key,
                k -> new HttpComponentsClientHttpRequestFactory(new DefaultHttpClientSupplier()
                        .setHttpClientConnectionManager(httpClientConnectionManager)
                        .setRequestConfig(requestConfig)
                        .setUseHttps(k.toLowerCase().startsWith("https://"))
                        .get()))
                .createRequest(uri, httpMethod);
    }

}
