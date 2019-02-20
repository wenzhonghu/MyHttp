package com.xiaoniu.finance.myhttp;

import com.xiaoniu.finance.myhttp.client.AbstractClient;
import com.xiaoniu.finance.myhttp.core.AbstractProxy;
import com.xiaoniu.finance.myhttp.core.DnsResolverManager;
import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import com.xiaoniu.finance.myhttp.util.StringUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhonghu on 2019/1/24.
 */
 class HttpConnectWrapper {

    private static final String S = "://";
    private static final String M = ":";
    private static final String X_ONLINE_HOST = "x-online-host";

    private AbstractClient mClient;
    private boolean isDnsEnable;

    public HttpConnectWrapper(AbstractClient client, boolean isDnsEnable) {
        this.mClient = client;
        this.isDnsEnable = isDnsEnable;
    }


    /**
     * 对外网络请求
     */
    public Response doHttpWrapper(Request request, AbstractProxy proxy) throws Throwable {
        if(!isDnsEnable){
            return doHttp(request);
        }
        /**
         * url转换为URI
         */
        URI uri;
        String url = request.getUrl();
        try {
            uri = new URI(request.getUrl());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            uri = null;
        }
        if (uri == null) {
            return Response.emptyResponse();
        }

        String host = DnsResolverManager.Instance().resolveDomainToIP(uri.getHost());
        if (proxy == null) {
            if (StringUtil.isNotTextEmpty(host)) {
                url = replaceHost(uri, host);
            }
        } else {
            url = replaceHost(uri, proxy.getHost());
            String xOnlineHost;
            if (StringUtil.isNotTextEmpty(host)) {
                xOnlineHost = host + M + uri.getPort();
            } else {
                xOnlineHost = uri.getAuthority();
            }
            if (StringUtil.isNotTextEmpty(xOnlineHost)) {
                Map<String, String> header = request.getHttpHead();
                if (header == null) {
                    header = new HashMap<String, String>(16);
                }
                header.put(X_ONLINE_HOST, xOnlineHost);
            }
        }
        request.setUrl(url);

        /**
         * url 可能是已经DNS改造过的地址
         * 真正网络请求
         */
        return doHttp(request);
    }

    /**
     * 域名替换ip
     *
     * @param uri 原域名地址
     * @param host ip主机地址
     */
    private String replaceHost(URI uri, String host) {
        return uri.getScheme() + S + host + M + uri.getPort() + uri.getPath();
    }

    private Response doHttp(Request request) throws Throwable {
        Response response = null;
        if (request.getHttpType() == Consts.HTTP_TYPE_GET) {
            // 进行get请求
            response = mClient.doGet(request);
        } else if (request.getHttpType() == Consts.HTTP_TYPE_POST) {
            // 进行post请求
            response = mClient.doPost(request);
        }
        return response;
    }


}
