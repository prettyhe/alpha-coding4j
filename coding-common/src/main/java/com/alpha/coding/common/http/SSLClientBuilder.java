package com.alpha.coding.common.http;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

/**
 * SSLClientBuilder
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class SSLClientBuilder {

    private SSLClientBuilder() {
    }

    public static HttpClientBuilder trustAllSSLClientBuilder() throws Exception {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        SSLContext sslContext = SSLContextBuilder.create().build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
        builder.setSSLSocketFactory(sslConnectionSocketFactory);
        return builder;
    }

    public static HttpClientBuilder trustAnySSLClientBuilder() throws Exception {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] {new TrustAnyTrustManager()}, new SecureRandom());
        SSLConnectionSocketFactory sslConnectionSocketFactory =
                new SSLConnectionSocketFactory(sslContext, new TrustAnyHostnameVerifier());
        builder.setSSLSocketFactory(sslConnectionSocketFactory);
        return builder;
    }

    @Deprecated
    public static class SSLClient extends DefaultHttpClient {
        public SSLClient() throws Exception {
            super();
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[] {tm}, null);
            org.apache.http.conn.ssl.SSLSocketFactory ssf = new org.apache.http.conn.ssl.SSLSocketFactory(ctx,
                    org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = this.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", 443, ssf));
        }
    }

    public static class TrustAnyTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public static class TrustAnyHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

}
