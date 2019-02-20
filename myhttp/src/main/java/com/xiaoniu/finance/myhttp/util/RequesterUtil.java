package com.xiaoniu.finance.myhttp.util;

import static com.xiaoniu.finance.myhttp.http.Consts.CHARSET_UTF;
import static com.xiaoniu.finance.myhttp.http.Consts.DATA_MAP;
import static com.xiaoniu.finance.myhttp.http.Consts.DATA_OBJET;
import static com.xiaoniu.finance.myhttp.http.Consts.DATE_STRING;

import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.http.request.Request;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 请求工具类
 */
public class RequesterUtil {


    /**
     *
     * @param request
     * @param postData
     * @return
     */
    public static Object getPostData(Request request, Map<String, String> postData) {
        if (request.getPostDataType() == DATE_STRING || request.getPostDataType() == DATA_OBJET) {
            return request.getPostData();
        } else if (request.getPostDataType() == DATA_MAP) {
            if (request.getPostData() != null) {
                postData.putAll((Map<String, String>) request.getPostData());
            }
            return postData;
        }
        return null;
    }


    /**
     * 根据请求数据类型进行数据解析
     */
    public static byte[] getPostData(Object postParam, int dataType) throws IOException {
        byte[] postContent = null;
        if (postParam != null) {
            switch (dataType) {
                case DATE_STRING:
                    postContent = ((String) postParam).getBytes();
                    break;
                case DATA_MAP:
                    String param = RequesterUtil.buildQuery((Map<String, String>) postParam, Consts.CHARSET_UTF, false);
                    postContent = param == null ? null : param.getBytes();
                    break;
                case Consts.DATA_BYTES:
                    postContent = (byte[]) postParam;
                    break;
            }
        }
        return postContent;
    }


    /**
     * 获取请求的消息体数据
     * @param uriParam
     * @param postParam
     * @param dataType
     * @return
     */
    public static String getPostData(Map<String, String> uriParam, Object postParam, int dataType){
        if(uriParam!=null && !uriParam.isEmpty()){
            return  RequesterUtil.mapToStr(uriParam);
        }
        if (postParam != null){
            switch (dataType) {
                case DATE_STRING:
                    return postParam.toString();
                case DATA_MAP:
                    return RequesterUtil.mapToStr((Map<String, String>) postParam);
                case Consts.DATA_BYTES:
                    try {
                        return new String((byte[]) postParam, CHARSET_UTF);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
            }
        }
        return "";
    }

    /**
     * 组建请求url
     */
    public static String buildRequestUrl(String url, Map<String, String> params, boolean hasSeparator) {
        try {
            String query = buildQuery(params, Consts.CHARSET_UTF, false);
            return buildURl(url, query, hasSeparator);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [URL参数拼接]<br/>
     */
    public static String buildURl(String strUrl, String query, boolean hasSeparator) throws MalformedURLException {
        URL url = new URL(strUrl);
        if (StringUtil.isTextEmpty(query)) {
            return strUrl;
        }

        if (StringUtil.isTextEmpty(url.getQuery())) {
            if (strUrl.endsWith(Consts.PARAM_SEQ)) {
                strUrl = strUrl + query;
            } else {
                if (hasSeparator) {
                    if (strUrl.endsWith(Consts.PARAM_FD)) {
                        strUrl = strUrl + Consts.PARAM_SEQ + query;
                    } else {
                        strUrl = strUrl + Consts.PARAM_FD + Consts.PARAM_SEQ + query;
                    }
                } else {
                    strUrl = strUrl + Consts.PARAM_SEQ + query;
                }
            }
        } else {
            if (strUrl.endsWith(Consts.PARAM_SPLIT)) {
                strUrl = strUrl + query;
            } else {
                strUrl = strUrl + Consts.PARAM_SPLIT + query;
            }
        }
        return strUrl;
    }


    /**
     * 根据参数Map来动态构造请求参数
     */
    public static String buildQuery(Map<String, String> params, String charset,
            boolean isEncoder) throws IOException {
        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder query = new StringBuilder();
        Set<Entry<String, String>> entries = params.entrySet();
        boolean hasParam = false;

        for (Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (name == null || name.length() == 0 || value == null
                    || value.length() == 0) {
                continue;
            }
            if (hasParam) {
                query.append(Consts.PARAM_SPLIT);
            } else {
                hasParam = true;
            }

            query.append(name).append(Consts.PARAM_EQ);
            if (isEncoder) {
                query.append(URLEncoder.encode(value, charset));
            } else {
                query.append(value);
            }
        }
        return query.toString();
    }

    public static String mapToStr(Map<String, String> map){
        if(map == null || map.isEmpty()){
            return  "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Entry<String, String> entry : map.entrySet()){
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append("|");
        }
        sb.append("]");
        return sb.toString();
    }
}

