package com.alpha.coding.common.http;

import java.util.function.Supplier;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * DefaultHttpClientSupplier
 *
 * @version 1.0
 * Date: 2020/6/9
 */
@Data
@Accessors(chain = true)
public class DefaultHttpClientSupplier implements Supplier<CloseableHttpClient> {

    private int retry;
    private boolean useHttps;
    private HttpClientConnectionManager httpClientConnectionManager;
    private RequestConfig requestConfig;

    @Override
    public CloseableHttpClient get() {
        if (useHttps) {
            try {
                if (retry > 0) {
                    return SSLClientBuilder.trustAllSSLClientBuilder()
                            .setRetryHandler(new BwopHttpRequestRetryHandler(retry))
                            .setConnectionManager(httpClientConnectionManager)
                            .setDefaultRequestConfig(requestConfig)
                            .build();
                } else {
                    return SSLClientBuilder.trustAllSSLClientBuilder()
                            .setConnectionManager(httpClientConnectionManager)
                            .setDefaultRequestConfig(requestConfig)
                            .build();
                }
            } catch (Exception e) {
                throw new RuntimeException("cannot get SSLClient");
            }
        } else {
            if (retry > 0) {
                return HttpClientBuilder.create()
                        .setRetryHandler(new BwopHttpRequestRetryHandler(retry))
                        .setConnectionManager(httpClientConnectionManager)
                        .setDefaultRequestConfig(requestConfig)
                        .build();
            } else {
                return HttpClientBuilder.create()
                        .setConnectionManager(httpClientConnectionManager)
                        .setDefaultRequestConfig(requestConfig)
                        .build();
            }
        }
    }

}
