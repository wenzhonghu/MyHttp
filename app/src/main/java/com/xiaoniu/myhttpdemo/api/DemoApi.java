package com.xiaoniu.myhttpdemo.api;

import static com.xiaoniu.myhttpdemo.client.Constants.BASE_URL;

import com.xiaoniu.finance.myhttp.HttpManager;
import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Request.RequestCacheType;
import com.xiaoniu.myhttpdemo.client.RequestCreator;
import com.xiaoniu.myhttpdemo.model.GeneralProjectResponse;
import com.xiaoniu.myhttpdemo.model.JsonParser;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhonghu on 2019/1/30.
 */

public class DemoApi {

    private static final String LOGIN_URL = "";

    /**
     * Login数据
     */
    public static void requestLogin(String requestId, OnRequestListener l) {
        Map<String, String> map = new HashMap<String, String>();
        String requestUrl = BASE_URL + LOGIN_URL;
        Request request = RequestCreator.createRequest(l);
        request.setHttpType(Consts.HTTP_TYPE_POST);
        request.setUrl(requestUrl);
        request.setUriParam(map);
        request.setTaskGroupdID(requestId);
        request.setParser(new JsonParser(GeneralProjectResponse.getParseType()));
        HttpManager.getInstance().doPost(request);
    }

    /**
     * test数据
     */
    public static void requestTest(String type, String requestId, OnRequestListener l) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("type", type);
        String requestUrl = BASE_URL + "xxxn.json";
        Request request = RequestCreator.createRequest(l);
        request.setHttpType(Consts.HTTP_TYPE_POST);
        request.setUrl(requestUrl);
        request.setUriParam(map);
        request.setTaskGroupdID(requestId);
        request.setCacheData(RequestCacheType.ClearAndUpdate);
        request.setParser(new JsonParser(GeneralProjectResponse.getParseType()));
        HttpManager.getInstance().doPost(request);
    }
}
