package com.xiaoniu.finance.myhttp.http.cookie;

import android.content.Context;
import java.util.List;

/**
 * cookie控制器
 * Created by zhonghu on 2019/2/19.
 */

public interface CookieController {

    CookieController NO_COOKIES = new CookieController() {
        @Override
        public void saveCookies(Context context, List<SaveCookies> cookies) {

        }

        @Override
        public List<SaveCookies> loadCookies(Context context) {
            return null;
        }
    };

    /**
     * 缓存cookie数据
     */
    void saveCookies(Context context, List<SaveCookies> cookies);

    /**
     * 加载cookie数据
     */
    List<SaveCookies> loadCookies(Context context);

}
