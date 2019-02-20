
package com.xiaoniu.finance.myhttp;

import static com.xiaoniu.finance.myhttp.http.Consts.DEFAULT_SOCKET_TIMEOUT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import com.xiaoniu.finance.myhttp.cache.DatabaseCacheController;
import com.xiaoniu.finance.myhttp.cache.ICacheController;
import com.xiaoniu.finance.myhttp.client.AbstractClient;
import com.xiaoniu.finance.myhttp.client.ClientVariableManager;
import com.xiaoniu.finance.myhttp.core.DnsResolverController;
import com.xiaoniu.finance.myhttp.core.DnsResolverManager;
import com.xiaoniu.finance.myhttp.filter.AbstractResponseFilter;
import com.xiaoniu.finance.myhttp.filter.BaseResponseFilter;
import com.xiaoniu.finance.myhttp.http.cookie.CookieController;
import com.xiaoniu.finance.myhttp.http.cookie.CookieHelper;
import com.xiaoniu.finance.myhttp.interceptor.AbstractInterceptor;
import com.xiaoniu.finance.myhttp.interceptor.BaseInterceptor;
import com.xiaoniu.finance.myhttp.statistic.StatisticProcesser;
import com.xiaoniu.finance.myhttp.statistic.StatisticProcesser.Mode;
import com.xiaoniu.finance.myhttp.statistic.processor.AbstractProcessor;
import com.xiaoniu.finance.myhttp.util.StringUtil;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * 全局公共数据
 * 1/提供context
 * 2/提供debug开关
 * 3/提供激活统计开关
 * 4/提供Builder模式
 */
public final class Global {

    public final static String TAG = "Global";

    private static boolean sIsInit = false;

    private static Context context;

    private static boolean isDebug = false;

    private static boolean enableStatistic = false;

    private static Builder builder;

    /**
     * 初始化工作
     * 必须第一步执行此方法
     *
     * @param ctx 上下文
     * @param enable 是否开启激活统计功能， true表示启动统计功能
     */
    public final static void init(Builder builder) {
        if (sIsInit) {
            return;
        }
        /**
         * 2/核心字段
         */
        Global.builder = builder;
        Global.context = builder.context;
        Global.enableStatistic = builder.enableStatistic;
        try {
            ApplicationInfo info = context.getApplicationInfo();
            Global.isDebug = ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
            if (Global.isDebug) {
                Log.w("Wns.Global.Runtime", "DEBUG is ON");
            }
        } catch (Exception e) {
            Global.isDebug = false;
        }
        /**
         * 3/初始化数据
         *  a/初始化全局对象
         *  b/初始化客户端参数
         *  c/初始化cookie参数
         *  d/初始化dns解析功能
         *  e/初始化网络参数
         */
        ClientVariableManager.getInstance().resetTrustCA(builder.appValidateHttpsCa, builder.appTrustCaStr);
        ClientVariableManager.getInstance().setTimeout(builder.connectionTimeout, builder.readConnectionTimeout);
        CookieHelper.getInstance().init(builder.serverUrl, builder.cookieController);
        DnsResolverManager.Instance().setDnsResolverController(builder.dnsResolverController);
        HttpConnectManager.getInstance().init(builder.context,
                builder.client, builder.iCacheController, builder.interceptor, builder.filter, builder.enableDns);
        /**
         * 4/启动域名解析工作
         * resolve domain
         */
        try {
            URI url = new URI(builder.serverUrl);
            DnsResolverManager.Instance().initURL(url.getHost());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        DnsResolverManager.Instance().startResolve();

        /**
         * 5/启动统计工作
         */
        if (builder.enableStatistic) {
            StatisticProcesser.getInstance().startProcessors();
        }

        Global.sIsInit = true;
    }

    public final static Context getContext() {
        return context;
    }

    public final static boolean isDebug() {
        return isDebug;
    }

    public final static Builder getBuilder() {
        return builder;
    }

    public final static boolean enableStatistic() {
        return enableStatistic;
    }

    public final static boolean isIsInit() {
        return sIsInit;
    }

    public final static Looper getMainLooper() {
        return getContext().getMainLooper();
    }

    public final static String getPackageName() {
        return getContext().getPackageName();
    }

    public final static File getFilesDir() {
        return getContext().getFilesDir();
    }

    public final static void sendBroadcast(Intent intent) {
        getContext().sendBroadcast(intent);
    }

    public final static Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return getContext().registerReceiver(receiver, filter);
    }

    public final static void unregisterReceiver(BroadcastReceiver receiver) {
        getContext().unregisterReceiver(receiver);
    }

    public final static Object getSystemService(String name) {
        return getContext().getSystemService(name);
    }

    public final static void revokeUriPermission(Uri uri, int modeFlags) {
        getContext().revokeUriPermission(uri, modeFlags);
    }


    /**
     * 　builder模式
     */
    public static final class Builder {

        private Context context;

        private int connectionTimeout;
        private int readConnectionTimeout;

        private boolean appValidateHttpsCa;
        private String[] appTrustCaStr;

        private ICacheController iCacheController;
        private AbstractClient client;
        private AbstractInterceptor interceptor;
        private AbstractResponseFilter filter;
        private DnsResolverController dnsResolverController;
        private List<Pair<Mode, AbstractProcessor>> processor;
        private CookieController cookieController;

        private String serverUrl;

        private boolean enableStatistic;
        private boolean enableDns;

        public Builder(Context context, String url) {
            this.context = context;
            this.serverUrl = url;
            this.connectionTimeout = DEFAULT_SOCKET_TIMEOUT;
            this.readConnectionTimeout = DEFAULT_SOCKET_TIMEOUT;
            this.appValidateHttpsCa = true;
            this.iCacheController = new DatabaseCacheController(context);
            this.interceptor = new BaseInterceptor();
            this.filter = new BaseResponseFilter();
            this.dnsResolverController = DnsResolverController.DEFALUT;
            this.enableStatistic = false;
            this.enableDns = true;
            this.processor = Collections.EMPTY_LIST;
            this.cookieController = CookieController.NO_COOKIES;
        }

        /**
         * 添加上下文
         */
        public Builder context(Context context) {
            if (context == null) {
                throw new NullPointerException("context == null");
            }
            this.context = context;
            return this;
        }

        /**
         * 添加cookie的缓存机制
         * @param cookieController
         * @return
         */
        public Builder cookieController(CookieController cookieController) {
            this.cookieController = cookieController;
            return this;
        }

        /**
         * 添加DNS功能开关
         *
         * @param enable true表示开功能
         */
        public Builder enableDns(boolean enable) {
            this.enableDns = enable;
            return this;
        }

        /**
         * 扩展DNS解析功能
         * 支持httpDNS等第三方
         * 默认情况使用系统DNS解析模块
         */
        public Builder dnsResolverController(DnsResolverController dnsResolverController) {
            this.dnsResolverController = dnsResolverController;
            return this;
        }

        /**
         * 添加激活统计功能开关
         *
         * @param enable true表示开启统计功能
         */
        public Builder enableStatistic(boolean enable) {
            this.enableStatistic = enable;
            return this;
        }

        /**
         * 添加统计处理器
         * 如果没有开启统计开关，此方法无效
         */
        public Builder addStatisticProcessor(Pair<Mode, AbstractProcessor> processor) {
            this.processor.add(processor);
            return this;
        }

        /**
         * 添加网络服务端请求地址
         */
        public Builder serverUrl(String serverUrl) {
            if (StringUtil.isTextEmpty(serverUrl)) {
                throw new NullPointerException("serverUrl == null");
            }
            this.serverUrl = serverUrl;
            return this;
        }

        /**
         * 连接超时，单位为ms
         */
        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * 读写超时，单位为ms
         */
        public Builder readConnectionTimeout(int readConnectionTimeout) {
            this.readConnectionTimeout = readConnectionTimeout;
            return this;
        }

        /**
         * 证书验证串数组
         */
        public Builder appTrustCaStr(String[] appTrustCaStr) {
            this.appTrustCaStr = appTrustCaStr;
            return this;
        }

        /**
         * 释放需要校验CA
         */
        public Builder appValidateHttpsCa(boolean appValidateHttpsCa) {
            this.appValidateHttpsCa = appValidateHttpsCa;
            return this;
        }

        /**
         * 添加缓冲控制器
         */
        public Builder cacheController(ICacheController cacheController) {
            this.iCacheController = iCacheController;
            return this;
        }

        /**
         * 添加网络请求器
         */
        public Builder client(AbstractClient client) {
            this.client = client;
            return this;
        }

        /**
         * 添加拦截器
         */
        public Builder interceptor(AbstractInterceptor interceptor) {
            this.interceptor = interceptor;
            return this;
        }

        /**
         * 添加过滤器
         */
        public Builder filter(AbstractResponseFilter filter) {
            this.filter = filter;
            return this;
        }
    }
}
