package com.alpha.coding.common.http;

import java.net.ProxySelector;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import com.alpha.coding.common.http.model.HttpConfig;
import com.alpha.coding.common.utils.StringUtils;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * DefaultHttpClientSupplier Builder for {@link CloseableHttpClient} instances.
 * <p>
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
 *
 * @version 1.0
 * Date: 2020/6/9
 */
@Data
@Accessors(chain = true)
public class DefaultHttpClientSupplier implements Supplier<CloseableHttpClient> {

    private boolean useHttps;
    private HttpClientConnectionManager httpClientConnectionManager;
    private RequestConfig requestConfig;
    private HttpConfig httpConfig;

    public static DefaultHttpClientSupplier create() {
        return new DefaultHttpClientSupplier();
    }

    @Override
    public CloseableHttpClient get() {
        final HttpClientBuilder httpClientBuilder;
        try {
            if (useHttps && httpConfig != null && httpConfig.isSslTrustAny()) {
                httpClientBuilder = SSLClientBuilder.trustAnySSLClientBuilder();
            } else if (useHttps) {
                httpClientBuilder = SSLClientBuilder.trustAllSSLClientBuilder();
            } else {
                httpClientBuilder = HttpClientBuilder.create();
            }
        } catch (Exception e) {
            throw new RuntimeException("Construct HttpClientBuilder fail", e);
        }
        httpClientBuilder.setConnectionManager(httpClientConnectionManager);
        httpClientBuilder.setDefaultRequestConfig(requestConfig);
        if (httpConfig != null && httpConfig.getRetry() > 0) {
            httpClientBuilder.setRetryHandler(new BwopHttpRequestRetryHandler(httpConfig.getRetry()));
        }
        if (httpConfig != null && StringUtils.isNotBlank(httpConfig.getHttpProxyHost())
                && StringUtils.isNotBlank(httpConfig.getHttpProxyUsername())) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(new AuthScope(httpConfig.getHttpProxyHost(), httpConfig.getHttpProxyPort()),
                    new UsernamePasswordCredentials(httpConfig.getHttpProxyUsername(),
                            httpConfig.getHttpProxyPassword()));
            httpClientBuilder.setDefaultCredentialsProvider(provider);
            httpClientBuilder.setProxy(new HttpHost(httpConfig.getHttpProxyHost(), httpConfig.getHttpProxyPort()));
        } else if (useDefaultProxy()) {
            SchemePortResolver schemePortResolver = DefaultSchemePortResolver.INSTANCE;
            httpClientBuilder.setSchemePortResolver(schemePortResolver);
            httpClientBuilder.setRoutePlanner(
                    new SystemDefaultRoutePlanner(schemePortResolver, ProxySelector.getDefault()));
            if (Stream.of("http.proxyUser", "https.proxyUser", "socks.proxyUser").map(System::getProperty)
                    .anyMatch(StringUtils::isNotBlank)) {
                httpClientBuilder.setDefaultCredentialsProvider(new SystemDefaultCredentialsProvider());
            }
        }
        if (httpConfig != null && StringUtils.isNotBlank(httpConfig.getUserAgent())) {
            httpClientBuilder.setUserAgent(httpConfig.getUserAgent());
        }
        return httpClientBuilder.build();
    }

    private boolean useDefaultProxy() {
        return "true".equals(System.getProperty("enableDefaultProxy"))
                && Stream.of("http.proxyHost", "https.proxyHost", "proxyHost", "socksProxyHost")
                .map(System::getProperty).anyMatch(StringUtils::isNotBlank);
    }

}
