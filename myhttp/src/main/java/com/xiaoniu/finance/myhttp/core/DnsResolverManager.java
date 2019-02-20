package com.xiaoniu.finance.myhttp.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.xiaoniu.finance.myhttp.Global;
import com.xiaoniu.finance.myhttp.core.Device.Network;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 域名管理
 * 如果多个地址，请按照默认的新增
 */
public class DnsResolverManager implements NetworkStateListener {

    private static final String TAG = "DnsResolverManager";


    //10ms
    private static final int SLEEP_INTERVAL = 10;
    private final static int THREAD_MAX_COUNT = 1;


    private static DnsResolverManager sDomainMgr = null;

    private String mKey = AccessPoint.NONE.getName();
    private ConcurrentHashMap<String, String> mDomainMap = new ConcurrentHashMap<String, String>();
    private ResolveThread[] mURLThreads = null;
    private String mURL = null;
    private ConnectivityManager mConnectivityMgr = null;
    private int currentType = -1;

    private DnsResolverController mDnsResolverController = DnsResolverController.DEFALUT;

    /**
     * 私有构造
     */
    private DnsResolverManager() {
        mURLThreads = new ResolveThread[THREAD_MAX_COUNT];
        Device.Network.addListener(this);
    }

    /**
     * 单例模式
     */
    public static synchronized DnsResolverManager Instance() {
        if (null == sDomainMgr) {
            sDomainMgr = new DnsResolverManager();
        }

        return sDomainMgr;
    }


    /**
     * 初始化域名管理
     */
    public void initURL(String url) {
        mURL = url;
    }

    /**
     * 启动解析域名线程
     */
    private synchronized ResolveThread startURLThread() {
        int i = 0;
        for (i = 0; i < THREAD_MAX_COUNT; i++) {
            if (mURLThreads[i] != null && mURLThreads[i].isAlive() == true) {
                if (mURLThreads[i].getKey() != mKey) {
                    mURLThreads[i].setExpired(true);
                } else {
                    //两个key相等，都为null时，也要设置当前线程失效
                    if (mKey != null) {
                        return mURLThreads[i];
                    } else {
                        mURLThreads[i].setExpired(true);
                    }
                }
            } else {
                mURLThreads[i] = new ResolveThread(mURL, mKey);
                mURLThreads[i].start();
                return mURLThreads[i];
            }
        }

        return null;
    }


    private ResolveThread startDnsThread(String domain) {
        if (domain != null && domain.length() > 0 && domain.equals(mURL)) {
            return startURLThread();
        }

        return null;
    }

    private String getKey() {
        String key = null;
        if (Network.isMobile()) {
            key = Network.getApnName();
        } else if (Network.isWifi()) {
            key = Network.Wifi.getBSSID();
        } else {
            // error
            Log.e(TAG, "getKey Network(" + Network.getType() + ") is unkown");
        }

        //如果获取到的bssid全为0，也不保存
        if (WifiDash.ZERO_BSSID.equals(key)) {
            key = null;
        }
        return key;
    }

    private boolean isNeedResolve() {
        String key = getKey();

        //如果key是空，强制要求重新解析
        if (key == null) {
            mKey = null;
            return true;
        }

        //如果key不相等
        if (key.equalsIgnoreCase(mKey) == false) {
            mKey = key;
            return true;
        }

        return false;
    }

    public void setDnsResolverController(DnsResolverController dnsResolverController) {
        this.mDnsResolverController = dnsResolverController;
    }


    /**
     * 启动DNS解析
     * 请先执行init初始化工作
     *
     * @see initURL（url）
     */
    public void startResolve() {
        if (!Network.isAvailable()) {
            return;
        }

        if (isNeedResolve()) {
            mDomainMap.clear();
            if (Network.isWifi()) {
                if (mURL != null && mURL.length() > 0) {
                    startURLThread();
                }
            }
        }
    }

    /**
     * 从缓存中查询已缓存的IP，为找到则返回domain
     */
    public String queryDomainForIP(String domain) {
        String ip = mDomainMap.get(domain);
        if (ip == null) {
            ip = domain;
        }
        return ip;
    }

    /**
     * 解析域名
     */
    public String resolveDomainToIP(String domain) {
        String ip = mDomainMap.get(domain);
        if (ip == null) {
            long timeout = 20000;
            long timepassed = 0;
            ResolveThread thread = startDnsThread(domain);
            if (thread == null) {
                return null;
            }
            while (true) {
                ip = mDomainMap.get(domain);
                if (ip == null) {
                    if (timepassed > timeout || thread.isCompleted()) {
                        break;
                    }

                    try {
                        Thread.sleep(SLEEP_INTERVAL);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "domain ip Exception", e);
                        return null;
                    }

                    timepassed += SLEEP_INTERVAL;
                } else {
                    return ip;
                }
            }
        }

        return ip;
    }

    private void setDomainIP(String domain, String ip) {
        mDomainMap.put(domain, ip);
    }

    private class ResolveThread extends Thread {

        private String mDomain = null;
        private volatile boolean mIsExpired = false;
        private volatile String mKey = null;
        private volatile boolean mIsCompleted = false;

        public ResolveThread(String domain, String key) {
            super("HttpDNS");
            mDomain = domain;
            mKey = key;
        }

        public void setExpired(boolean isExprired) {
            mIsExpired = isExprired;
        }

        public String getKey() {
            return mKey;
        }

        public boolean isCompleted() {
            return mIsCompleted;
        }

        @Override
        public void run() {
            String ip;
            mIsCompleted = false;

            ip = mDnsResolverController.dnsResolver(mDomain);

            if (ip != null && mIsExpired == false) {
                setDomainIP(mDomain, ip);
            }

            mIsCompleted = true;
        }
    }

    /**
     * 当网络切换则重新解析DNS
     *
     * @param lastState 之前的网络状态
     * @param newState 现在的网络状态
     */
    @Override
    public void onNetworkStateChanged(NetworkState lastState, NetworkState newState) {
        try {
            if (mConnectivityMgr == null) {
                mConnectivityMgr = (ConnectivityManager) Global.getSystemService(Context.CONNECTIVITY_SERVICE);
            }

            NetworkInfo networkInfo = mConnectivityMgr.getActiveNetworkInfo();
            Log.i(TAG, "NetworkChangeReceiver " + networkInfo);

            if (networkInfo != null && networkInfo.isConnected()) {
                if (currentType != networkInfo.getType()) {
                    DnsResolverManager.Instance().startResolve();
                    currentType = networkInfo.getType();
                }
            } else {
                currentType = -1;
            }
        } catch (Exception e) {
            currentType = -1;
            Log.e(TAG, "Get networkInfo fail", e);
        }
    }


}
