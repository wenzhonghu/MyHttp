package com.xiaoniu.finance.myhttp.cache;


import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;

/**
 * 缓存接口
 */

public interface ICacheController {

    /**
     * 从缓存器获取响应数据
     * @param request
     */
    void getResponseFromCache(Request request);

    /**
     * 把响应数据缓存到缓存器中
     * @param result
     * @param request
     * @param response
     */
    void saveResponseToCache(Object result, Request request, Response response);

    /**
     * 是否缓存
     * @param request
     * @return
     */
    boolean isCache(Request request);


    /**
     * 清空当前请求的缓存
     */
    void clearCache(Request request);


    /**
     * 清空所有缓存，
     * 本质上删除缓存表
     */
    void clearAllCache();

}
