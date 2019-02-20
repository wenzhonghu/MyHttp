package com.xiaoniu.finance.myhttp;

import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.monitor.AbstractMonitor;
import java.util.Map;

/**
 * Created by zhonghu on 2019/1/25.
 */

public final class HttpManager {

    private static final String TAG = HttpManager.class.getSimpleName();


    private volatile static HttpManager mHttpManager;

    public static HttpManager getInstance() {
        return mHttpManager;
    }


    private HttpManager() {
    }

    //------------------------------------------可以真正干活的方法----------------------------------

    /**
     * 执行get请求，数据为request的uriParam属性值
     *
     * @see init(Builder builder)
     */
    public static void doGet(Request request) {
        HttpConnectManager.getInstance().doGet(request);
    }

    /**
     * 执行post请求，数据为request的uriParam属性值
     * 注意：需要先执行init初始化工作
     *
     * @see init(Builder builder)
     */
    public static void doPost(Request request) {
        HttpConnectManager.getInstance().doPost(request);
    }

    /**
     * 执行post请求，数据为Map对象
     * 注意：需要先执行init初始化工作
     *
     * @param postParam Map对象
     * @see init(Builder builder)
     */
    public static void doPost(Request request, Map<String, String> postParam) {
        HttpConnectManager.getInstance().doPost(request, postParam);
    }

    /**
     * 执行post请求，数据为字符串
     * 注意：需要先执行init初始化工作
     *
     * @param postParam 字符串
     * @see init(Builder builder)
     */
    public static void doPost(Request request, String postParam) {
        HttpConnectManager.getInstance().doPost(request, postParam);
    }

    /**
     * 执行post请求，数据为字节流数组
     * 注意：需要先执行init初始化工作
     *
     * @param postParam 字节流数组
     * @see init(Builder builder)
     */
    public static void doPost(Request request, byte[] postParam) {
        HttpConnectManager.getInstance().doPost(request, postParam);
    }

    /**
     * 执行post请求，数据为对象
     * 注意：需要先执行init初始化工作
     *
     * @param postParam 对象数据
     * @see init(Builder builder)
     */
    public static void doPost(Request request, Object postParam) {
        HttpConnectManager.getInstance().doPost(request, postParam);
    }

    //---------------------------------------------------------------------------------------------

    /**
     * 增加请求监听器<BR>
     * 所有的Http请求得到结果的时候都会回调到监听器
     */
    public static void registerMonitor(AbstractMonitor monitor) {
        HttpConnectManager.getInstance().registerMonitor(monitor);
    }

    /**
     * 移除
     */
    public boolean unRegisterMonitor(AbstractMonitor monitor) {
        return HttpConnectManager.getInstance().unRegisterMonitor(monitor);
    }


    /**
     * 释放数据
     */
    public synchronized static void release() {
        if (mHttpManager != null) {

        }
        HttpConnectManager.release();
        mHttpManager = null;
    }
}
