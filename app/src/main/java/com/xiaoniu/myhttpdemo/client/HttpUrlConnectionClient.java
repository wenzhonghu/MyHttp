package com.xiaoniu.myhttpdemo.client;

import static com.xiaoniu.finance.myhttp.util.RequesterUtil.getPostData;

import android.text.TextUtils;
import android.util.Log;
import com.xiaoniu.finance.myhttp.client.AbstractClient;
import com.xiaoniu.finance.myhttp.client.trust.JavaSSLSocketFactory;
import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import com.xiaoniu.finance.myhttp.http.request.Response.Builder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by zhonghu on 2019/1/30.
 */

public class HttpUrlConnectionClient implements AbstractClient {

    public static final String TAG = "HttpUrlConnectionClient";

    public static final String DEFAULT_CHARSET = "utf-8";

    public static final String METHOD_POST = "POST";

    public static final String METHOD_GET = "GET";

    public static final int CONNECTION_TIMEOUT = 30 * 1000;

    public static final String GZIP_ENCODING = "gzip";// gzip的encode名称

    private static final boolean isPrintHeadInfo = false;

    @Override
    public Response doGet(Request request) throws Throwable {
        return null;
    }

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

        byte[] postContent = requestData.getBytes();
        return connection(url, Consts.METHOD_POST, postContent, httpHeader);
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
    private static Response connection(String url, String method,
            byte[] postContent, Map<String, String> httpHead) throws IOException {

        HttpURLConnection conn = null;
        OutputStream out = null;

        try {
            conn = getConnection(new URL(url), method, httpHead);
            if (conn instanceof HttpsURLConnection) {
                ((HttpsURLConnection) conn).setSSLSocketFactory(JavaSSLSocketFactory.getSslSocketFactory());
                ((HttpsURLConnection) conn).setHostnameVerifier(JavaSSLSocketFactory.getHostnameVerifier());
            }
            if (postContent != null) {
                conn.setDoOutput(true);
                out = conn.getOutputStream();
                if (out != null) {
                    out.write(postContent);
                }
            }
            conn.connect();
            InputStream inputStream = null;
            int responseCode = conn.getResponseCode();
            if (!isErrorRequest(responseCode)) {
                String encoding = conn.getContentEncoding();
                Log.i(TAG, "connection - responseCode: " + responseCode);

                if (encoding != null && encoding.contains(GZIP_ENCODING)) {
//                    inputStream = new MultiMemberGZIPInputStream(
//                            conn.getInputStream());
                } else {
                    inputStream = conn.getInputStream();
                }
                Response resp;
                Builder builder = new Builder();

                Map<String, String> resultMap = new HashMap<String, String>();
                String rsp = getStreamAsString(inputStream, "utf-8");

                Log.v(TAG, "结果:" + rsp);
                getHttpHeadMap(conn, resultMap);
                return builder.code(responseCode).header(resultMap).data(rsp).build();
            } else {
                return Response.onlyCodeResponse(responseCode);
            }


        } catch (IOException e) {
            Log.e(TAG, "connection - IOException:" + e.getLocalizedMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


    public static void getHttpHeadMap(HttpURLConnection conn,
            Map<String, String> map) {
        Map<String, List<String>> headmap = conn.getHeaderFields();
        if (headmap == null) {
            return;
        }
        Iterator<Entry<String, List<String>>> it = headmap.entrySet()
                .iterator();
        while (it.hasNext()) {
            Entry<String, List<String>> entry = it.next();
            String headKey = entry.getKey();
            List<String> headValueList = entry.getValue();
            String headValue = null;
            if (headValueList != null && headValueList.size() > 0) {
                headValue = headValueList.get(0);
                map.put(headKey, headValue);
                if (isPrintHeadInfo) {
                    Log.v(TAG, "[HEAD]Key:" + headKey + " - Value:" + headValue);
                }
            }
        }
    }

    /**
     * 得到Http连接
     */
    public static HttpURLConnection getConnection(URL url, String method,
            Map<String, String> httpHead) throws IOException {
        HttpURLConnection conn = null;

        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setReadTimeout(CONNECTION_TIMEOUT);
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("osType", Constants.HttpHeader.OS_TYPE);
        conn.setRequestProperty("User-Agent", Constants.HttpHeader.USER_AGENT);
        conn.setRequestProperty("Content-Type",
                "application/json;charset=" + DEFAULT_CHARSET);
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("Accept-Encoding", "gzip");

        if (httpHead != null) {
            Iterator<String> it = httpHead.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = httpHead.get(key);

                if (key != null && value != null) {
                    conn.setRequestProperty(key, value);
                }

                Log.v(TAG, "Head:" + key + " - " + value);
            }
        }

        return conn;
    }

    /**
     * 将流转换成字符
     */
    public static String getStreamAsString(InputStream stream, String charset)
            throws IOException {

        String resultString = "";

        if (stream == null || charset == null) {
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream, charset));
            StringWriter writer = new StringWriter();
            char[] chars = new char[256];
            int count = 0;
            while ((count = reader.read(chars)) > 0) {
                writer.write(chars, 0, count);
            }

            resultString = writer.toString();
            // Log.v(TAG, "返回的结果: "+ resultString);
            return resultString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (stream != null) {

                stream.close();
            }
        }
    }

    public static byte[] getStreamAsBytes(InputStream stream)
            throws IOException {

        if (stream == null) {
            return null;
        }

        int num = -1;
        byte[] buf = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((num = stream.read(buf, 0, buf.length)) != -1) {
            baos.write(buf, 0, num);
        }
        byte[] b = baos.toByteArray();
        baos.flush();
        baos.close();
        return b;
    }


    private static boolean isErrorRequest(int responseCode) {
        return responseCode >= HttpURLConnection.HTTP_BAD_REQUEST;
    }

}
