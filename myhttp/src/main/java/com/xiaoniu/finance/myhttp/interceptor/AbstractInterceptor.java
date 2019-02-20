package com.xiaoniu.finance.myhttp.interceptor;

import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;

/**
 * 拦截器
 * 真正网络执行前后才能执行此拦截器
 * Created by zhonghu on 2019/1/24.
 */

public interface AbstractInterceptor {

    /**
     * http运行前的拦截器
     * @param request
     */
    void beforeInterceptor(Request request);

    /**
     * http运行后的拦截器
     * 可能出现需要异步执行，则自己去实现
     * 例如异步缓存数据等
     * @param request
     * @param response
     */
    void afterInterceptor(Request request, Response response);

}
