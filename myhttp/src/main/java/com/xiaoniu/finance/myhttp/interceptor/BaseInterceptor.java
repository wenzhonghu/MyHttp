package com.xiaoniu.finance.myhttp.interceptor;

import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;

/**
 * 默认不实现
 * Created by zhonghu on 2019/1/27.
 */

public class BaseInterceptor implements AbstractInterceptor {

    @Override
    public void beforeInterceptor(Request request) {

    }

    @Override
    public void afterInterceptor(Request request, Response response) {

    }
}
