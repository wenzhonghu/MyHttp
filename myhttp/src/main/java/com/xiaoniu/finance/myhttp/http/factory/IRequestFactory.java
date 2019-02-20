package com.xiaoniu.finance.myhttp.http.factory;


import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;

/**
 * 生成request对象
 */

public interface IRequestFactory {

    Request createRequest(OnRequestListener listener, String cookie, String userAgent);
}
