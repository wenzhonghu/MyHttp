package com.xiaoniu.finance.myhttp.client.trust;

/**
 * Created by zhonghu on 2019/1/25.
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * 使用自定义验证方式
 * VerifierTrustManager
 */
class MySSLSocketFactory extends SSLSocketFactory {

    private SSLContext sslContext;

    public MySSLSocketFactory() {
        super();
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new VerifierTrustManager()}, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return new String[0];
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return sslContext.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return sslContext.getSocketFactory().createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return sslContext.getSocketFactory().createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return sslContext.getSocketFactory().createSocket(address, port, localAddress, localPort);
    }

}
