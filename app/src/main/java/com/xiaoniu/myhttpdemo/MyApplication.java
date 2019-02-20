package com.xiaoniu.myhttpdemo;

import android.app.Application;
import com.xiaoniu.finance.myhttp.Global;
import com.xiaoniu.finance.myhttp.Global.Builder;
import com.xiaoniu.finance.myhttp.filter.MultipleResponseFilter;
import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.myhttpdemo.client.Constants;
import com.xiaoniu.myhttpdemo.client.HttpUrlConnectionClient;
import com.xiaoniu.myhttpdemo.cookie.AppCookieController;
import com.xiaoniu.myhttpdemo.filter.LoginFilter;


/**
 * Created by zhonghu on 2019/1/29.
 */

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Builder builder = new Builder(this, Constants.BASE_URL)
                .enableStatistic(true)
                .appTrustCaStr(Constants.TRUST_CA_STR)
                .appValidateHttpsCa(true)
                .connectionTimeout(Consts.DEFAULT_SOCKET_TIMEOUT)
                .readConnectionTimeout(Consts.DEFAULT_SOCKET_TIMEOUT)
                .filter(new LoginFilter())
                //.filter(new MultipleResponseFilter().addFilter(new LoginFilter()))
                .cookieController(new AppCookieController())
                .enableDns(false)
                //.dnsResolverController(new HttpDnsResolverController())
                //.client(new OkHttpClient())
                .client(new HttpUrlConnectionClient());
        Global.init(builder);
    }
}
