package com.xiaoniu.finance.myhttp.http.cookie;

import com.xiaoniu.finance.myhttp.http.cookie.CookieManager.Cookie;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 缓存cookie
 * Created by zhonghu on 2019/2/19.
 */
public class SaveCookies {

    public String key;
    public CopyOnWriteArrayList<Cookie> cookies;
}
