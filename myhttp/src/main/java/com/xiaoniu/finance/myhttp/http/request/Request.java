package com.xiaoniu.finance.myhttp.http.request;

import static com.xiaoniu.finance.myhttp.http.Consts.DATA_MAP;
import static com.xiaoniu.finance.myhttp.http.Consts.PRIOPITY_MAX;
import static com.xiaoniu.finance.myhttp.http.Consts.PRIOPITY_MIN;
import static com.xiaoniu.finance.myhttp.http.Consts.PRIOPITY_NORMAL;

import android.content.Context;
import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.http.parser.IDataParser;
import com.xiaoniu.finance.myhttp.util.RequesterUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络请求参数封装
 */
public class Request {

    /**
     * 上下文
     */
    private Context responseOnUiContext;

    /**
     * Http类型，Post、Get
     */
    private int httpType;

    /**
     * Post参数类型 String、Map
     */
    private int postDataType = DATA_MAP;


    /**
     * 请求URL
     */
    private String url;

    /**
     * Get请求参数
     */
    private Map<String, String> uriParam;

    /**
     * Http头信息
     */
    private Map<String, String> httpHead;

    /**
     * Http Post参数
     */
    private Object postData;

    /**
     * 请求类型，可以用于标示同一个URL请求处理不同事情
     */
    private int requestType;

    /**
     * 监听器
     */
    private OnRequestListener listener;

    private Object tag;
    private Map<String, Object> tags;

    /**
     * 该请求的请求时长 只有当使用HttpConnectionManager的时候，在请求结束后才有值
     */
    private long requestTime;

    /**
     * 数据解析器
     */
    private IDataParser parser;

    private int priority = PRIOPITY_NORMAL;

    private boolean runBackground;

    /**
     * 是否缓存返回的成功数据,默认不缓存
     */
    private RequestCacheType isCacheData = RequestCacheType.None;

    /**
     * 任务组的ID 可以根据组ID批量清除正在任务队列的任务
     */
    private String taskGroupdID;

    /**
     * 请求是否被中断
     */
    private boolean isCancelReqesut = false;

    /**
     * 是否正在请求中
     */
    private boolean isRequesting = false;

    /**
     * 请求取消监听
     */
    private OnCancelListener onCancelListener;


    /**
     * 禁止当前请求执行过滤器功能
     * 默认不禁用
     */
    private boolean isForbidFilter = false;

    /**
     * 当前请求执行是否为同步还是异步
     * 默认走异步
     */
    private boolean isSupportAsync = true;


    public Request() {
    }

    public Request(String url) {
        this.url = url;
    }


    /**
     *
     * @param priority
     */
    public void setPriority(int priority) {
        if (priority < PRIOPITY_MAX || priority > PRIOPITY_MIN) {
            return;
        }
        this.priority = priority;
    }

    /**
     * 获取完整的请求地址
     */
    public String getRequestEntireUrl() {
        String requestUrl = getUrl();
        try {
            String query = RequesterUtil.buildQuery(getUriParam(), Consts.CHARSET_UTF, true);
            requestUrl = RequesterUtil.buildURl(getUrl(), query, false);
        } catch (Exception e) {
        }
        return requestUrl;
    }

    /**
     * 是否取消请求操作
     */
    public void cancelReqesut(boolean isCancel) {
        this.isCancelReqesut = isCancel;

        if (onCancelListener != null) {
            onCancelListener.onRequestCancel(this);
        }
    }

    /**
     * 设置标签tag
     */
    public void setTag(String key, Object tag) {
        if (tags == null) {
            tags = new HashMap<String, Object>();
        }
        tags.put(key, tag);
    }

    public Object getTag(String key) {
        if (tags == null) {
            return null;
        }
        return tags.get(key);
    }

    /**
     * 是否缓存返回的成功数据
     */
    public void setCacheData(RequestCacheType isCacheData) {
        this.isCacheData = isCacheData;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getUriParam() {
        return uriParam;
    }

    public void setUriParam(Map<String, String> uriParam) {
        this.uriParam = uriParam;
    }

    public Object getPostData() {
        return postData;
    }

    public void setPostData(Object postData) {
        this.postData = postData;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public int getHttpType() {
        return httpType;
    }

    public void setHttpType(int httpType) {
        this.httpType = httpType;
    }

    public int getPostDataType() {
        return postDataType;
    }

    public void setPostDataType(int postDataType) {
        this.postDataType = postDataType;
    }

    public Map<String, String> getHttpHead() {
        return httpHead;
    }

    public void setHttpHead(Map<String, String> httpHead) {
        this.httpHead = httpHead;
    }

    public void setOnRequestListener(OnRequestListener l) {
        this.listener = l;
    }

    public OnRequestListener getOnRequestListener() {
        return listener;
    }

    public IDataParser getParser() {
        return parser;
    }

    public void setParser(IDataParser parser) {
        this.parser = parser;
    }

    public int getPriority() {
        return priority;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public boolean isRunBackground() {
        return runBackground;
    }

    public void setRunBackground(boolean runBackground) {
        this.runBackground = runBackground;
    }

    public String getTaskGroupdID() {
        return taskGroupdID;
    }

    public void setTaskGroupdID(String taskGroupdID) {
        this.taskGroupdID = taskGroupdID;
    }

    public boolean isCancelReqesut() {
        return isCancelReqesut;
    }

    public boolean isRequesting() {
        return isRequesting;
    }

    public void setRequesting(boolean isRequesting) {
        this.isRequesting = isRequesting;
    }

    public void setResponseOnUiContext(Context context) {
        this.responseOnUiContext = context;
    }

    public Context getResponseOnUiContext() {
        return this.responseOnUiContext;
    }

    public OnCancelListener getOnCancelListener() {
        return onCancelListener;
    }

    public void setOnCancelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    public RequestCacheType isCacheData() {
        return isCacheData;
    }


    public boolean isForbidFilter() {
        return isForbidFilter;
    }

    public void setForbidFilter(boolean forbidFilter) {
        isForbidFilter = forbidFilter;
    }


    public boolean isSupportAsync() {
        return isSupportAsync;
    }

    public void setSupportAsync(boolean supportAsync) {
        isSupportAsync = supportAsync;
    }

    /**
     * 请求缓存类型
     */
    public enum RequestCacheType {
        /**
         * 不缓存
         */
        None(0),
        /**
         * 一直缓存，手工清空缓存
         */
        Always(1),
        /**
         * 获取缓存后然后下次重新更新获取缓存
         */
        ClearAndUpdate(2);

        private int type;

        RequestCacheType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
