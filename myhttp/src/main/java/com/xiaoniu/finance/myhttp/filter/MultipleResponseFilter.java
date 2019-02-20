package com.xiaoniu.finance.myhttp.filter;

import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * 多个拦截器实现
 * <p/>
 * 注意：
 * 此时的conform方法不能充当开关效果，所以默认全局执行通过
 * 其开关可以通过filter方法中实现
 * Created by zhonghu on 2019/1/27.
 */

public class MultipleResponseFilter extends AbstractResponseFilter {

    private List<AbstractResponseFilter> mFilters;

    public MultipleResponseFilter() {
        mFilters = new ArrayList<>();
    }

    @Override
    public boolean conform(Request request, Response response) {
        for (AbstractResponseFilter filter : mFilters) {
            filter.conform(request, response);
        }
        return true;
    }

    @Override
    public void filter(String url, int state, Object result, int type, Request request, Response response, OnRequestListener listener) {
        for (AbstractResponseFilter filter : mFilters) {
            filter.filter(url, state, result, type, request, response, listener);
        }
    }

    /**
     * 添加拦截器
     */
    public MultipleResponseFilter addFilter(AbstractResponseFilter filter) {
        this.mFilters.add(filter);
        return this;
    }
}
