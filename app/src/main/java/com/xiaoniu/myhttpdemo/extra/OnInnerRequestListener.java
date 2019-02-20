package com.xiaoniu.myhttpdemo.extra;


import com.xiaoniu.finance.myhttp.http.request.OnRequestListener;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.http.request.Response;
import com.xiaoniu.myhttpdemo.extra.event.BaseMessageEvent;
import java.lang.reflect.Field;
import org.greenrobot.eventbus.EventBus;

/**
 * 回调包装类
 */
public class OnInnerRequestListener<E extends BaseMessageEvent.ResponseEvent> implements OnRequestListener {

    private static final String URL = "url";
    private static final String STATE = "state";
    private static final String RESULT = "result";
    private static final String TYPE = "type";
    private static final String REQUEST = "request";
    private static final String RESPONSE = "response";
    private static final String DATA = "data";
    private Object mData;
    private E mBaseResponseEvent;
    private EventBus mEventBus;

    public OnInnerRequestListener() {
    }

    public OnInnerRequestListener(E event) {
        this.mBaseResponseEvent = event;
    }

    public OnInnerRequestListener setExtraData(Object data) {
        this.mData = data;
        return this;
    }

    public void setEventBus(EventBus eventBus) {
        this.mEventBus = eventBus;
    }

    @Override
    public void onResponse(String url, int state, Object result, int type, Request request, Response response) {
        Class c = mBaseResponseEvent.getClass();
        setFieldValue(c, URL, url);
        setFieldValue(c, STATE, state);
        setFieldValue(c, RESULT, result);
        setFieldValue(c, TYPE, type);
        setFieldValue(c, REQUEST, request);
        setFieldValue(c, RESPONSE, response);
        setFieldValue(c, DATA, mData);

        if (mEventBus == null) {
            EventBus.getDefault().post(mBaseResponseEvent);
        } else {
            mEventBus.post(mBaseResponseEvent);
        }


    }

    private <V> void setFieldValue(Class c, String field, V value) {
        try {
            Field urlField = c.getField(field);
            urlField.setAccessible(true);
            urlField.set(mBaseResponseEvent, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
