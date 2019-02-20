package com.xiaoniu.finance.myhttp.filter;

import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;

/**
 * Created by zhonghu on 2019/1/27.
 */

public class BaseResponseFilter extends AbstractResponseFilter {

    @Override
    public boolean conform(Request request, Response response) {
        return true;
    }

    @Override
    public void filter(String url, int state, Object result, int type, Request request, Response response, OnRequestListener listener) {

    }
}
