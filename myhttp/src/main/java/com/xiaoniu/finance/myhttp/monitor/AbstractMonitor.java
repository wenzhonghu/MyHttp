package com.xiaoniu.finance.myhttp.monitor;

import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import java.util.Map;

/**
 * 响应监听器，
 * 实现当存在网络时则唤醒所有注册过此监听的网络请求
 */
public interface AbstractMonitor {

    /**
     *
     * @param url
     * @param state
     * @param result
     * @param type
     * @param request
     * @param response
     */
    void onMonitor(String url, int state, Object result, int type, Request request, Response response);


}

