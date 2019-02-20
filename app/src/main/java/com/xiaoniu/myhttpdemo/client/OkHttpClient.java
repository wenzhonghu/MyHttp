package com.xiaoniu.myhttpdemo.client;

import static com.xiaoniu.finance.myhttp.http.Consts.CONTENT_TYPE;
import static com.xiaoniu.finance.myhttp.util.RequesterUtil.getPostData;

import android.text.TextUtils;
import android.util.Log;
import com.xiaoniu.finance.myhttp.client.AbstractClient;
import com.xiaoniu.finance.myhttp.client.ClientVariableManager;
import com.xiaoniu.finance.myhttp.client.trust.JavaSSLSocketFactory;
import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.http.Consts.HttpHeader;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import com.xiaoniu.finance.myhttp.http.request.Response.Builder;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhonghu on 2019/1/21.
 */

public class OkHttpClient implements AbstractClient {

    private static final String TAG = "OkHttpClient";

    public static final okhttp3.MediaType JSON = okhttp3.MediaType.parse(CONTENT_TYPE);

    private static okhttp3.OkHttpClient mClient;

    public OkHttpClient() {

        try {

            mClient = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(ClientVariableManager.getInstance().getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(ClientVariableManager.getInstance().getReadConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(ClientVariableManager.getInstance().getReadConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .sslSocketFactory(JavaSSLSocketFactory.getSslSocketFactory(), JavaSSLSocketFactory.getVerifierTrustManager())
                    .hostnameVerifier(JavaSSLSocketFactory.getHostnameVerifier())
                    .addInterceptor(new OkHttpExceptionInterceptor())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * okhttp AsyncCall only catch the IOException, other exception will occur crash，this Interceptor impl can transform all exception to IOException
     */
    private static class OkHttpExceptionInterceptor implements okhttp3.Interceptor {

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            try {
                return chain.proceed(chain.request());
            } catch (Throwable e) {
                if (e instanceof IOException) {
                    throw e;
                } else {
                    throw new IOException(e);
                }
            }
        }
    }


    @Override
    public Response doGet(Request request) throws Throwable {
        return null;
    }

    /**
     *
     * @param request
     * @return
     * @throws Throwable
     */
    @Override
    public Response doPost(Request request) throws Throwable {
        if (request == null || TextUtils.isEmpty(request.getRequestEntireUrl())) {
            return null;
        }

        Map<String, String> postData = new HashMap<String, String>();
        String url = getUrlAndParems(request, postData);
//		String url = requestInfo.getRequestEntireUrl();
        Log.i(TAG, "doPost:" + url);
        Map<String, String> httpHeader = new HashMap<String, String>(request.getHttpHead());
        String requestData = getRequestData(getPostData(request, postData), httpHeader);

        return connection(url, Consts.METHOD_POST, requestData, httpHeader);
    }


    private String getUrlAndParems(Request request, Map<String, String> result) {
        String[] urlAndParams = request.getUrl().split("\\?");
        if (request.getUriParam() != null) {
            result.putAll(request.getUriParam());
        }
        if (urlAndParams.length == 2 && !TextUtils.isEmpty(urlAndParams[1])) {
            keyValueStr2Map(result, urlAndParams[1].split("\\&"));
        }
        return urlAndParams[0];
    }


    private String getRequestData(Object data, Map<String, String> httpHeader) {
        //TODO
        return "";
    }


    private void keyValueStr2Map(Map<String, String> map, String[] keyValues) {
        if (map == null || keyValues == null || keyValues.length <= 0) {
            return;
        }
        for (String keyV : keyValues) {
            if (TextUtils.isEmpty(keyV)) {
                continue;
            }
            String[] headerKeyValue = keyV.split("\\=");
            if (headerKeyValue.length == 2) {
                map.put(headerKeyValue[0].trim(), headerKeyValue[1].trim());
            }
        }
    }


    /**
     * doPost请求方法
     */
    private static Response connection(String url, String method, String postContent, Map<String, String> header) throws IOException {
        if (header == null) {
            header = new HashMap<String, String>();
        }
        header.put("Accept", "*/*");
        header.put("osType", HttpHeader.OS_TYPE);
        header.put("User-Agent", HttpHeader.USER_AGENT);
        header.put("connection", "Keep-Alive");
        header.put("Content-Type", CONTENT_TYPE);

        okhttp3.Headers headers = okhttp3.Headers.of(header);

        Response resp;
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url).headers(headers)
                .method(method, okhttp3.RequestBody.create(JSON, postContent))
                .build();

        okhttp3.Response response = mClient.newCall(request).execute();
        if (response.isSuccessful()) {
            Builder builder = new Builder();
            okhttp3.Headers responseHeaders = response.headers();

            Map<String, String> respHeaders = new HashMap<String, String>();
            for (int i = 0; i < responseHeaders.size(); i++) {
                respHeaders.put(responseHeaders.name(i), responseHeaders.value(i));
            }
            String content = response.body().string();
            Log.v(TAG, "结果:" + content);

            resp = builder.header(respHeaders).code(response.code()).data(content).build();
        } else {
            resp = Response.onlyCodeResponse(response.code());
        }
        response.close();
        return resp;

    }

}
