package com.xiaoniu.myhttpdemo.extra.event;

import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import java.util.Map;

/**
 * 供eventBus post的实体事件类
 * 定义所需的实体类
 *
 * @author 许蛟
 * @date 2016年5月11日
 */
public class BaseMessageEvent {

    public static class BaseResponseEvent<C> {

        public C tag;
    }

    public static class RefreshEvent extends BaseResponseEvent {

        public String pageType;

        public RefreshEvent(String pageType) {
            this.pageType = pageType;
        }
    }


    /**
     * 公共返回的response
     */
    public static class ResponseEvent extends BaseResponseEvent {

        public String url;
        public int state;
        public Object result;
        public int type;
        public Request request;
        public Response response;
        public Object data;
    }

    /**
     * 列表集合
     */
    public static class ListResponseEvent extends ResponseEvent {

    }

    /**
     * 公共返回的cancelRequest
     */
    public static class CancelRequestEvent extends BaseResponseEvent {
        public Request request;
    }


}
