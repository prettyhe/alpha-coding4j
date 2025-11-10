package com.alpha.coding.common.http.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * HttpConfig
 *
 * @version 1.0
 * @date 2024年06月26日
 */
@Data
@Accessors(chain = true)
public class HttpConfig implements Serializable {

    private boolean sslTrustAny;
    private String sslContextType;
    private String[] supportedProtocols;
    private String[] supportedCipherSuites;
    private int connTimeout;
    private int soTimeout;
    private int retry;
    private String httpProxyHost;
    private int httpProxyPort;
    private String httpProxyUsername;
    private String httpProxyPassword;
    private String userAgent;

    public static HttpConfig create() {
        return new HttpConfig();
    }

}
