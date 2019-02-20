package com.xiaoniu.finance.myhttp.monitor;

import android.util.Log;
import com.xiaoniu.finance.myhttp.HttpManager;
import com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求重试管理器
 *
 * @author zhonghu
 */

public class RetryRequestMonitor implements AbstractMonitor {

    public static final String TAG = RetryRequestMonitor.class.getSimpleName();

    private static RetryRequestMonitor sInstance = new RetryRequestMonitor();

    private Map<String, RetryRecord> mMonitorMap = new ConcurrentHashMap<String, RetryRecord>();

    private RetryRequestMonitor() {
        HttpManager.getInstance().registerMonitor(this);
    }

    public static RetryRequestMonitor getInstance() {
        return sInstance;
    }


    /**
     * [注册重试请求器]
     * 注册进来的请求器会在每一次其他接口请求成功后自动触发它
     * 如果注册后没有注销，在其他接口成功请求后总是会触发它被执行，它无法判断你需要的重试接口是否被成功处理
     * 是否需要重试或不重试，需要你自己的regist和unRegist来告诉它
     */
    public synchronized void registRetryApi(String key, IRetryRequester requester) {
        if (mMonitorMap.containsKey(key)) {
            return;
        }
        Log.v(TAG, "registRetryApi key:" + key);
        RetryRecord record = new RetryRecord();
        record.url = key;
        record.requester = requester;
        mMonitorMap.put(key, record);
    }

    public synchronized void unRegistRetryApi(String key) {
        Log.d(TAG, "unRegistRetryApi key:" + key);
        mMonitorMap.remove(key);
    }

    private boolean clear() {
        if (mMonitorMap != null) {
            mMonitorMap.clear();
        }
        mMonitorMap = null;
        return true;
    }

    /**
     * 主动执行重试请求操作
     */
    public void request() {
        /**
         * url为空则第一步不执行
         * STATE_SUC则第二步不执行
         * 直接跳转到最后一步执行
         */
        doRetryRequest(null, HttpRespCode.STATE_SUC);
    }

    /**
     * 被动执行重试请求操作
     * 当被注册到请求中会被动触发
     */
    @Override
    public synchronized void onMonitor(String url, int state, Object result, int type, Request request, Response response) {
        doRetryRequest(url, state);
    }

    /**
     * 重试请求
     */
    private void doRetryRequest(String url, int state) {
        Iterator<Entry<String, RetryRecord>> it = mMonitorMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, RetryRecord> entry = it.next();
            RetryRecord record = entry.getValue();
            /**
             * 第一步
             */
            if (url != null && url.equals(record.url)) {
                record.isRequesting = false;
                Log.i(TAG, "doRetryRequest 以下Url请求结束:" + url);
                continue;
            }

            /**
             * 第二步
             */
            if (record.isRequesting) {
                Log.w(TAG, "doRetryRequest 该请求正在进行中，不进行重复请求 " + record.url);
                continue;
            }

            /**
             * 第三步
             */
            if (state != HttpRespCode.STATE_SUC) {
                continue;
            }
            /**
             * 最后一步执行
             */
            Log.v(TAG, "doRetryRequest 发起请求重试:" + record.url + " - 触发者：" + url);
            record.requester.doRequest();
            record.isRequesting = true;
        }
    }

    /**
     * 重试记录
     */
    class RetryRecord {

        String url;
        IRetryRequester requester;
        boolean isRequesting = false;
    }

    /**
     * 对外提供重试请求
     */
    public interface IRetryRequester {

        boolean doRequest();
    }
}
