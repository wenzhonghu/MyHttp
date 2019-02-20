package com.xiaoniu.myhttpdemo.client;


import com.xiaoniu.finance.myhttp.http.factory.DefaultRequestFactory;
import com.xiaoniu.finance.myhttp.http.factory.IRequestFactory;
import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;

/**
 */
public class RequestCreator {

    private static final String TAG = "RequestCreator";

    public static Request createRequest(OnRequestListener listener) {

        IRequestFactory defaultRequestFactory = DefaultRequestFactory.createRequestFactory();

        String cookie = "";
        String userAgent = "test";

        Request request = defaultRequestFactory.createRequest(listener, cookie, userAgent);

        return request;
    }


}
