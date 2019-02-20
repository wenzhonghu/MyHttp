package com.xiaoniu.finance.myhttp;

import static android.util.Log.v;
import static com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode.STATE_EXCEPTION;
import static com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode.STATE_NO_NETWORK;
import static com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode.STATE_SSL_HANDSHARE;
import static com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode.STATE_SUC;
import static com.xiaoniu.finance.myhttp.http.Consts.HttpRespCode.STATE_TIME_OUT;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import com.xiaoniu.finance.myhttp.cache.ICacheController;
import com.xiaoniu.finance.myhttp.client.AbstractClient;
import com.xiaoniu.finance.myhttp.core.AbstractProxy;
import com.xiaoniu.finance.myhttp.core.Device.Network;
import com.xiaoniu.finance.myhttp.core.Device.Network.ProxyMode;
import com.xiaoniu.finance.myhttp.core.NetworkDash;
import com.xiaoniu.finance.myhttp.filter.AbstractResponseFilter;
import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.http.Consts.CmdCode;
import com.xiaoniu.finance.myhttp.http.parser.IDataParser;
import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Request.RequestCacheType;
import com.xiaoniu.finance.myhttp.http.request.Response;
import com.xiaoniu.finance.myhttp.interceptor.AbstractInterceptor;
import com.xiaoniu.finance.myhttp.monitor.AbstractMonitor;
import com.xiaoniu.finance.myhttp.statistic.PushMessage;
import com.xiaoniu.finance.myhttp.statistic.StatisticHandler;
import com.xiaoniu.finance.myhttp.statistic.profile.BizProfile;
import com.xiaoniu.finance.myhttp.task.AbstractTaskRunnable;
import com.xiaoniu.finance.myhttp.task.TaskExecutorManager;
import com.xiaoniu.finance.myhttp.task.TaskPollExecutor;
import com.xiaoniu.finance.myhttp.util.PostUiRunnable;
import com.xiaoniu.finance.myhttp.util.RequesterUtil;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.net.ssl.SSLHandshakeException;


/**
 * [Http网络请求管理器]
 * 网络以队列进行请求，请求池中最小会维持1个线程等待任务，最多同时执行10个请求任务，当30秒内没有大于1个请求的请求数量时会将当前的请求池缩减到1
 * 网络请求的各种配置在Request类中进行，可设置请求的回调、解析、请求URL、Header、Post参数等
 * 在请求回调中可以得到该次请求的属性如：数据结果、服务器返回的Header、请求状态、请求耗时
 * void onResponse(String url, int state, Object result, int type, Request request, Response response);
 * 注意：该回调是异步请求回调，不在UI主线程执行（考虑到请求返回后可能会进行一些耗时处理，在主线程会导致又需要再开一个线程进行，
 * 这种业务的个性需求由业务调用决定）
 */
class HttpConnectManager {

    public static final String TAG = "HttpConnectManager";

    private static HttpConnectManager instance;

    private Handler mHandler;
    private Context mContext;

    private ProxyMode proxyMode = ProxyMode.NeverTry;
    private ICacheController mCacheController;
    private AbstractClient mClient;
    private AbstractInterceptor mInterceptor;
    private AbstractResponseFilter mFilter;
    private List<AbstractMonitor> mMonitors;
    private boolean mEnableDns;

    private HttpConnectManager() {
    }

    /**
     * 初始化方法
     * 在应用启动时初始化
     */
    void init(Context context, AbstractClient client, ICacheController cacheController, AbstractInterceptor interceptor, AbstractResponseFilter filter, boolean enableDns) {
        if (context == null) {
            return;
        }
        mContext = context;
        mClient = client;
        mCacheController = cacheController;
        mInterceptor = interceptor;
        mFilter = filter;
        mEnableDns = enableDns;

        if (mHandler == null) {
            mHandler = new Handler(mContext.getMainLooper());
        }

    }

    /**
     * 常规数据请求队列管理器<BR>
     */
    public static synchronized HttpConnectManager getInstance() {
        if (instance == null) {
            instance = new HttpConnectManager();
        }
        return instance;
    }

    //--------------------------------------------------------------------------------------------------

    /**
     * 清理请求操作
     */
    private static void clearRequest() {
        TaskExecutorManager.getInstance(TaskExecutorManager.TYPE_HTTP).removeAllTask();
        TaskExecutorManager.getInstance(TaskExecutorManager.TYPE_HTTP_LOW_PRIORITY).removeAllTask();
    }

    /**
     * 清理请求同时释放内存
     */
    public static void release() {
        if (instance != null) {
            instance.release0();
            instance = null;
        }
        clearRequest();
    }

    private void release0() {
        mContext = null;
        mCacheController = null;
        mClient = null;
        mMonitors.clear();
        mMonitors = null;
        mInterceptor = null;
    }

    //--------------------------------------------------------------------------------------------------

    /**
     * [Get请求]<br/>
     *
     * @param request 请求参数
     */
    public void doGet(Request request) {
        if (request == null) {
            return;
        }
        request.setHttpType(Consts.HTTP_TYPE_GET);
        connection(request);
    }

    /**
     * [Post请求]<br/>
     * 默认以map方式post
     *
     * @param request 请求参数
     */
    public void doPost(Request request) {
        if (request == null) {
            return;
        }
        request.setHttpType(Consts.HTTP_TYPE_POST);
        request.setPostDataType(Consts.DATA_MAP);
        connection(request);
    }

    /**
     * [Post请求]<br/>
     *
     * @param request 请求参数
     * @param postParam Post参数
     */
    public void doPost(Request request, Map<String, String> postParam) {
        if (request == null) {
            return;
        }
        request.setHttpType(Consts.HTTP_TYPE_POST);
        request.setPostDataType(Consts.DATA_MAP);
        request.setPostData(postParam);
        connection(request);
    }

    public void doPost(Request request, String postParam) {
        if (request == null) {
            return;
        }
        request.setHttpType(Consts.HTTP_TYPE_POST);
        request.setPostDataType(Consts.DATE_STRING);
        request.setPostData(postParam);
        connection(request);
    }

    public void doPost(Request request, byte[] postParam) {
        if (request == null) {
            return;
        }
        request.setHttpType(Consts.HTTP_TYPE_POST);
        request.setPostDataType(Consts.DATA_BYTES);
        request.setPostData(postParam);
        connection(request);
    }

    public void doPost(Request request, Object postParam) {
        if (request == null) {
            return;
        }
        request.setHttpType(Consts.HTTP_TYPE_POST);
        request.setPostDataType(Consts.DATA_OBJET);
        request.setPostData(postParam);
        connection(request);
    }

    //--------------------------------------------------------------------------------------------------

    /**
     * 添加网络请求到请求队列
     */
    private void connection(Request request) {
        AbstractTaskRunnable runnable = new ConnectionRunnable(request);
        runnable.setPriority(request.getPriority());
        runnable.setTaskGroupdID(request.getTaskGroupdID());
        if (request.isSupportAsync()) {
            if (request.isRunBackground()) {
                TaskExecutorManager.getInstance(TaskExecutorManager.TYPE_HTTP_LOW_PRIORITY).execute(runnable);
            } else {
                TaskExecutorManager.getInstance(TaskExecutorManager.TYPE_HTTP).execute(runnable);
            }
        } else {
            runnable.run();
        }
    }

    /**
     * 根据请求的组Id进行批量移除任务
     * 只会对处理任务处理队列的任务才能有效清除，不对正在执行中的任务进行影响
     */
    private void removeTaskByGroup(String taskGroupID) {
        TaskPollExecutor executor;
        executor = TaskExecutorManager.getInstance(TaskExecutorManager.TYPE_HTTP_LOW_PRIORITY);
        executor.removeTask(taskGroupID);

        executor = TaskExecutorManager.getInstance(TaskExecutorManager.TYPE_HTTP);
        executor.removeTask(taskGroupID);
    }

    /**
     * 网络请求线程
     */
    class ConnectionRunnable extends AbstractTaskRunnable {

        Request request;

        ConnectionRunnable(Request request) {
            this.request = request;
            setPriority(request.getPriority());
            setTaskID(UUID.randomUUID().toString());
            setTaskGroupdID(request.getTaskGroupdID());
        }


        @Override
        public String toString() {
            return request.toString();
        }

        @Override
        public void onCancel() {
            request.cancelReqesut(true);
        }

        @Override
        public void run() {
            request.setRequesting(true);
            requestConnection();
            request.setRequesting(false);
        }

        private void requestConnection() {

            /**
             * 开关开启同时已经提供缓存时，则执行缓存操作
             */
            if (mCacheController != null && request.isCacheData() != RequestCacheType.None) {
                if (mCacheController.isCache(request)) {
                    if (request.isCacheData() == RequestCacheType.Always) {
                        mCacheController.getResponseFromCache(request);
                        return;
                    } else {
                        mCacheController.getResponseFromCache(request);
                        mCacheController.clearCache(request);
                        return;
                    }
                }

            }

            /**
             * 网络操作
             */
            realRequestConnection(request);
        }

        /**
         * 真正的网络请求
         */
        private void realRequestConnection(final Request request) {
            int state = STATE_EXCEPTION;
            final String url = request.getUrl();
            String requestUrl = request.getRequestEntireUrl();
            long t1 = SystemClock.elapsedRealtime();
            if (isCancel()) {
                return;
            }

            /**
             * 网络请求操作
             */
            Response response = null;
            HttpConnectWrapper wrapper = new HttpConnectWrapper(mClient, mEnableDns);
            try {
                if (mInterceptor != null) {
                    mInterceptor.beforeInterceptor(request);
                }
                response = doRealHttp(wrapper, request);
                if (mInterceptor != null) {
                    mInterceptor.afterInterceptor(request, response);
                }
            } catch (SSLHandshakeException e) {
                state = STATE_SSL_HANDSHARE;
            } catch (IOException e) {
                if (NetworkDash.isAvailable()) {
                    state = STATE_TIME_OUT;
                } else {
                    state = STATE_NO_NETWORK;
                }
            } catch (Throwable e) {
                state = STATE_EXCEPTION;
            }

            if (response == null) {
                response = Response.onlyCodeResponse(state);
            } else {
                int responseCode = response.getHttpRetCode();
                if (responseCode == STATE_SUC) {
                    state = STATE_SUC;
                } else {
                    state = responseCode;
                }
            }

            /**
             * 响应数据结果处理
             */
            Object result = null;
            if (state == STATE_SUC) {
                result = response.getHttpRetData();
                v(TAG, "result: " + result);
            }

            long t2 = SystemClock.elapsedRealtime();
            long time = t2 - t1;
            request.setRequestTime(time);

            if (isCancel()) {
                return;
            }

            /**
             * 有效的结果数据
             */
            if (state == STATE_SUC && result != null) {
                IDataParser parser = request.getParser();

                if (parser != null) {
                    try {
                        result = parser.parseData(result.toString());
                        v(TAG, "parseData: " + result);
                    } catch (Exception e) {
                        result = null;
                        e.printStackTrace();
                    }
                    try {
                        if (result != null) {
                            /**
                             * 执行缓存操作
                             */
                            if (mCacheController != null && request.isCacheData() != RequestCacheType.None) {
                                mCacheController.saveResponseToCache(result, request, response);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (isCancel()) {
                return;
            }
            /**
             * 如果满足执行过滤功能则启动，否则原路执行
             */
            if (!request.isForbidFilter() &&
                    mFilter != null && mFilter.conform(request, response)) {
                mFilter.setListener(request.getOnRequestListener());
                request.setOnRequestListener(mFilter);
            }

            /**
             * 回调原路结果出去
             */
            callback(request, state, response, url, result);

            /**
             * 同时处理监听队列的数据
             */
            callMonitor(requestUrl, state, result, request.getRequestType(), request, response);

            /**
             * 统计功能
             */
            if (Global.enableStatistic()) {
                try {
                    statistic(t1, time, request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 统计功能
         */
        private void statistic(long startTime, long requestTime, Request request, Response response) {
            BizProfile p = new BizProfile();
            p.startTaskTime = startTime;
            p.requestTime = requestTime;
            p.url = request.getUrl();
            p.httpType = request.getHttpType();
            p.priority = request.getPriority();
            p.taskGroupID = request.getTaskGroupdID();
            p.runBackground = request.isRunBackground();
            p.isCacheData = request.isCacheData().getType();
            p.cacheType = String.valueOf(request.isCacheData().getType());
            p.isForbidFilter = request.isForbidFilter();
            p.responseCode = response.getHttpRetCode();
            p.responseMsg = response.getHttpRetMsg();

            p.requestHeader = RequesterUtil.mapToStr(request.getHttpHead());
            p.requestData = RequesterUtil.getPostData(request.getUriParam(), request.getPostData(), request.getPostDataType());
            p.responseHeader = RequesterUtil.mapToStr(response.getHttpRetHeader());
            p.responseData = response.getHttpRetData();

            PushMessage message = new PushMessage(CmdCode.HTTP_CMDID, BizProfile.parseResult(p));
            StatisticHandler.getInstance().handleRecvMessage(message);
        }


        /**
         * 根据是否传递context来实现哪个线程去执行请求结果的调用
         */
        private void callback(final Request request, int state, Response response, final String url, Object result) {
            final OnRequestListener listener = request.getOnRequestListener();
            if (listener != null) {
                Context resonseUiContext = request.getResponseOnUiContext();
                if (resonseUiContext != null) {
                    final int state2 = state;
                    final Object result2 = result;
                    final Response response2 = response;
                    mHandler.post(new PostUiRunnable(resonseUiContext) {
                        @Override
                        public void postRun() {
                            if (isCancel()) {
                                return;
                            }
                            listener.onResponse(url, state2, result2, request.getRequestType(), request, response2);
                        }
                    });
                } else {
                    listener.onResponse(url, state, result, request.getRequestType(), request, response);
                }
            }
        }
    }


    /**
     * 实现连接模式
     * 没尝试过：尝试直连，成功了就认为永久直连，失败了就再试一次代理，成功了就永久代理，失败了下次继续尝试
     */
    private Response doRealHttp(HttpConnectWrapper wrapper, Request request) throws Throwable {
        Response response;
        switch (proxyMode) {
            // 没尝试过：尝试直连，成功了就认为永久直连，失败了就再试一次代理，成功了就永久代理，失败了下次继续尝试
            case NeverTry: {
                response = wrapper.doHttpWrapper(request, null);
                boolean isSuccess = response != null && response.isSuccess();
                if (isSuccess) {
                    proxyMode = ProxyMode.Direct;
                    return response;
                } else {
                    if (Network.isWap()) {
                        response = wrapper.doHttpWrapper(request, AbstractProxy.Default);
                        isSuccess = response != null && response.isSuccess();
                        if (isSuccess) {
                            proxyMode = ProxyMode.ViaProxy;
                        }
                    }
                }
            }
            break;
            // 已经直连：那么直连
            case Direct: {
                response = wrapper.doHttpWrapper(request, null);
                break;
            }
            // 已经代理，那么代理
            case ViaProxy: {
                response = wrapper.doHttpWrapper(request, AbstractProxy.Default);
                break;
            }
            default:
                response = null;
        }
        return response;
    }


    /**
     * 增加请求监听器<BR>
     * 所有的Http请求得到结果的时候都会回调到监听器
     */
    public void registerMonitor(AbstractMonitor monitor) {
        if (monitor == null) {
            return;
        }
        if (mMonitors == null) {
            mMonitors = new CopyOnWriteArrayList<AbstractMonitor>();
        }
        if (mMonitors.contains(monitor)) {
            return;
        }
        mMonitors.add(monitor);
    }

    /**
     * 移除
     */
    public boolean unRegisterMonitor(AbstractMonitor monitor) {
        if (monitor == null || mMonitors == null) {
            return false;
        }
        Object remove = mMonitors.remove(monitor);
        return remove != null;
    }

    private void callMonitor(String url, int state, Object result, int type, Request request, Response response) {
        if (mMonitors == null) {
            return;
        }
        for (AbstractMonitor monitor : mMonitors) {
            monitor.onMonitor(url, state, result, type, request, response);
        }
    }
}
