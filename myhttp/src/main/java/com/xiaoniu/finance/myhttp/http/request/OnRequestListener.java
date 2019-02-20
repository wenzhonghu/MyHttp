package com.xiaoniu.finance.myhttp.http.request;


/**
 * 请求监听
 */
public interface OnRequestListener {

    /**
     * Http请求返回响应<br/>
     *
     * @param url 请求Url<br/>
     * @param state 请求状态<br/> STATE_CALL_ERROR 调用错误<br/> STATE_EXCEPTION 崩溃异常<br/> STATE_SUC 请求成功<br/> STATE_TIME_OUT 请求超时<br/> STATE_ERROR_RESULT Http返回码不为200<br/>
     * @param result 返回数据
     * @param type 请求类型，与发送请求时候传输的type一样
     * @param request 请求体
     * @param response 服务器响应信息
     */
    void onResponse(String url, int state, Object result, int type, Request request, Response response);

    /**
     * 可观察的请求监听
     * 链式反应
     * 听过两个方式：一个式构造函数传递，一个式通过set方式
     */
    abstract class ObserverableRequestListener implements OnRequestListener {

        private OnRequestListener listener;

        public ObserverableRequestListener(){}

        public ObserverableRequestListener(OnRequestListener srcListener) {
            this.listener = srcListener;
        }

        public void setListener(OnRequestListener listener){
            this.listener = listener;
        }

        @Override
        public void onResponse(String url, int state, Object result, int type, Request request, Response response) {
            todo(url, state, result, type, request, response, listener);
            if (this.listener != null) {
                this.listener.onResponse(url, state, result, type, request, response);
            }
        }

        /**
         * 二次开花
         */
        public abstract void todo(String url, int state, Object result, int type, Request request, Response response, OnRequestListener srcListener);
    }

}
