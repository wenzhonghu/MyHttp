package com.xiaoniu.finance.myhttp.http.cookie;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.xiaoniu.finance.myhttp.util.StringUtil;
import java.util.Map;


/**
 * 生成业务cookie
 * 一般cookie用于保存用户数据信息<p/>
 * 简单理解：
 * 服务端<----->客户端 通过session保存用户信息（token）<br/>
 * 客户端<----->浏览器 通过cookie保存用户信息（cookie）<br/>
 *
 * @author zhonghu
 */
public class CookieHelper {

    private static final String TAG = CookieHelper.class.getSimpleName();
    private static final Object obj = new Object();
    private static CookieHelper mCookieHelper;

    private String mUrl;

    private CookieHelper() {
    }

    public static CookieHelper getInstance() {
        if (mCookieHelper == null) {
            mCookieHelper = new CookieHelper();
        }
        return mCookieHelper;
    }


    public void init(String url, CookieController cookieController) {
        this.mUrl = url;
        CookieManager.getInstance().addController(cookieController);
    }

    /**
     * Web服务器通过发送一个称为 Set-Cookie 的 HTTP 消息头来创建一个 cookie，Set-Cookie 消息头是一个字符串，
     * 通过 Set-Cookie 指定的可选项只会在浏览器端使用，而不会被发送至服务器端。
     * 发送至服务器的 cookie 的值与通过 Set-Cookie 指定的值完全一样，不会有进一步的解析或转码操作。
     * 如果请求中包含多个 cookie，它们将会被分号和空格分开
     *
     * @param userCookie 用户自定义的cookie数据
     * @param headerMap 如果userCookie没传为空则从服务端的头部Set-Cookie获取
     */
    public void createCookie(Context context, String userCookie, Map<String, String> headerMap) {
        String cookie = null;
        if (StringUtil.isTextEmpty(userCookie)) {
            if (headerMap != null && !headerMap.isEmpty()
                    && headerMap.containsKey("Set-Cookie")) {
                cookie = headerMap.get("Set-Cookie");
            }
        } else {
            cookie = userCookie;
        }
        if (StringUtil.isNotTextEmpty(cookie)) {
            synCookies(context, cookie);
        }
    }

    /**
     * 　同步cookies
     * 如果请求中包含多个 cookie，它们将会被分号和空格分开
     */
    public void synCookies(Context context, String cookie) {
        synCookies(context, mUrl, cookie);
    }

    /**
     * 　同步cookies
     * 如果请求中包含多个 cookie，它们将会被分号和空格分开
     */
    private void synCookies(Context context, String url, String cookie) {
        /**
         * CookieManager以","分割
         */
        if (!TextUtils.isEmpty(cookie)) {
            cookie = cookie.replaceAll(";", "\u002c");
        }
        Log.v(TAG, "synCookies mUrl:" + url + " mCookie: " + cookie);
        synchronized (obj) {
            CookieManager.getInstance().removeCookie(url);
            CookieManager.getInstance().setCookie(url, cookie);
            CookieManager.getInstance().saveCookieFile(context);
        }
        Log.v(TAG, "synCookies cookie: " + cookie);
    }


    /**
     *
     * @param context
     * @return
     */
    public String readCookie(Context context) {
        CookieManager.getInstance().readCookieFile(context);
        String cookie = CookieManager.getInstance().getCookie(mUrl);
        Log.v(TAG, "readCookie cookie: " + cookie);
        return cookie;
    }

    /**
     *
     * @param context
     */
    public void removeCookie(Context context) {
        CookieManager.getInstance().removeCookie(mUrl);
        CookieManager.getInstance().saveCookieFile(context);
    }

    public void removeAllCookie(Context context) {
        CookieManager.getInstance().removeAllCookie();
        CookieManager.getInstance().saveCookieFile(context);
    }

    /**
     * 是否在Sdcard中存储着User的Cookie数据
     */
    public boolean hasSaveCookie(String url) {
        String cookie = CookieManager.getInstance().getCookie(url);
        return !StringUtil.isTextEmpty(cookie);
    }

}
