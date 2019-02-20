package com.xiaoniu.myhttpdemo.filter;

import static com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode.STATE_SSL_HANDSHARE;
import static com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode.STATE_SUC;

import android.widget.Toast;
import com.xiaoniu.finance.myhttp.filter.AbstractResponseFilter;
import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import com.xiaoniu.myhttpdemo.LoginActivity;
import com.xiaoniu.myhttpdemo.client.Constants;
import com.xiaoniu.myhttpdemo.model.BaseResponse;

/**
 * Created by zhonghu on 2019/1/30.
 */

public class LoginFilter extends AbstractResponseFilter {

    /**
     * 符合条件则执行过滤器功能
     * 默认为不符合条件情况，如果需要请重写此方法
     * 例如返回响应对象的code是M000120错误时需要重新登陆
     */
    public boolean conform(Request request, Response response) {
        //final com.xiaoniu.myhttpdemo.model.BaseResponse resp = (com.xiaoniu.myhttpdemo.model.BaseResponse) response.getHttpRetObject();
        return true;
    }


    /**
     * TODO
     * @param url
     * @param state
     * @param result
     * @param type
     * @param request
     * @param response
     * @param listener
     */
    @Override
    public void filter(String url, int state, Object result, int type, Request request, Response response, OnRequestListener listener) {
        if (state == STATE_SSL_HANDSHARE) {
            //握手失败，CA证书验证可能出现了问题
//            if (AccountManager.getInstance().isLogin()) {
//                AccountManager.getInstance().logout(false, listener);
//            }
            Toast.makeText(request.getResponseOnUiContext(), "握手失败，CA证书验证可能出现了问题", Toast.LENGTH_SHORT).show();
            return;

        }

        if (state != STATE_SUC || result == null) {
            return;
        }
        if (!(result instanceof BaseResponse)) {
            return;
        }

        final BaseResponse resp = (BaseResponse) result;
        if (resp.isSuccess() || resp.code == null) {
            return;
        }
        if (Constants.ResultCode.RESPONSE_CODE_LOGIN_TIMEOUT.equals(resp.code)) {// 登录失效
            LoginActivity.startMe(request.getResponseOnUiContext());
        }
    }
}
