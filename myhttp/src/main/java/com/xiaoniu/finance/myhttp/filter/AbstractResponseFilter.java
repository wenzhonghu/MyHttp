package com.xiaoniu.finance.myhttp.filter;

import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.OnRequestListener.ObserverableRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;

/**
 * 全局拦截器
 * 用于特殊业务，例如请求返回登陆token失效需要跳转到登陆界面操作
 * <p/>
 * 注意：
 * 如果业务只需要单个拦截器则简单实现即可,如果多个则可通过组合模式实现多个拦截器{@link MultipleResponseFilter}
 * @author zhonghu
 */

public abstract class AbstractResponseFilter extends ObserverableRequestListener {

    /**
     * 符合条件则执行过滤器功能
     * 默认为不符合条件情况，如果需要请重写此方法
     * 例如返回响应对象的code是M000120错误时需要重新登陆
     */
    public boolean conform(Request request, Response response) {
        return false;
    }


    /**
     * 执行过滤功能
     * 需要必备满足两个条件：
     * 一个是conform方法返回true，即当前满足过滤功能的条件，例如返回响应对象的code是M000120错误时需要重新登陆
     * 一个是请求对象的isForbidFilter字段为false，即当前请求不走过滤器
     */
    public abstract void filter(String url, int state, Object result, int type, Request request, Response response, OnRequestListener listener);

    @Override
    public void todo(String url, int state, Object result, int type, Request request, Response response, OnRequestListener srcListener) {
        filter(url, state, result, type, request, response, srcListener);
    }

}
