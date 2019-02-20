package com.xiaoniu.finance.myhttp.client.trust;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.AbstractVerifier;

/**
 * 证书认证Socket，防止狸猫换太子
 * 注意，在Fiddler下的HttpsUrlConnection会出现Socket Close的请求失败，需要关掉代理才能正常访问，属于Fiddler的bug
 */
public class JavaSSLSocketFactory {


    private final static SSLSocketFactory sslSocketFactory = new MySSLSocketFactory();

    private static final AbstractVerifier allowAllHostnameVerifier = new AllowAllHostnameVerifier();

    private static final X509TrustManager trustManager = new VerifierTrustManager();


    public static X509TrustManager getVerifierTrustManager() {
        return trustManager;
    }

    public static AbstractVerifier getHostnameVerifier() {
        return allowAllHostnameVerifier;
    }

    public static SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }


}
