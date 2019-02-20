package com.xiaoniu.finance.myhttp.http.factory;

import static com.xiaoniu.finance.myhttp.http.Consts.HttpHeader.KEY_COOKIE;
import static com.xiaoniu.finance.myhttp.http.Consts.HttpHeader.KEY_USER_AGENT;

import android.text.TextUtils;
import com.xiaoniu.finance.myhttp.Global;
import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;
import java.util.HashMap;
import java.util.Map;


/**
 * 实现默认的请求对象
 */
public class DefaultRequestFactory implements IRequestFactory {

    private DefaultRequestFactory() {
    }

    @Override
    public Request createRequest(OnRequestListener listener, String cookie, String userAgent) {
        Request request = new Request();

        request.setOnRequestListener(listener);
        Map<String, String> headerMap = new HashMap<String, String>();
        request.setHttpHead(headerMap);

        if (!TextUtils.isEmpty(cookie)) {
            headerMap.put(KEY_COOKIE, cookie);
        }

        //添加User-Agent
        if (!TextUtils.isEmpty(userAgent)) {
            headerMap.put(KEY_USER_AGENT, userAgent);
        }
        //用于网络请求版本兼容
        headerMap.put("Xne", "lite");

        //设置上下文
        request.setResponseOnUiContext(Global.getContext());

        return request;
    }

    public static IRequestFactory createRequestFactory() {
        return new DefaultRequestFactory();
    }


}
