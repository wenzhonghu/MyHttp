package com.xiaoniu.finance.myhttp.client;

import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import java.util.Map;

/**
 * [网络请求客户端]
 * @author zhonghu
 */
public interface AbstractClient {

    /**
     * get请求
     * @param request
     * @return
     * @throws Throwable
     */
    Response doGet(Request request) throws Throwable;

    /**
     * post请求
     * @param request
     * @return
     * @throws Throwable
     */
    Response doPost(Request request) throws Throwable;
}

