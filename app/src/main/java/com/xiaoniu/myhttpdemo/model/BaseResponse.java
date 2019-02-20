package com.xiaoniu.myhttpdemo.model;

import com.google.gson.reflect.TypeToken;
import com.xiaoniu.myhttpdemo.client.Constants;
import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * 数据响应格式
 */
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 119940790462712533L;

    public static Type getParseType() {
        return new TypeToken<BaseResponse<?>>() {
        }.getType();
    }

    public static final String TAG = "BaseResponse";

    public String code;

    public String message;

    public T data;

    public long serverTime;

    public boolean isSuccess() {
        return Constants.ResultCode.SUCCESS.equals(code);
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data + '\'' +
                ", serverTime=" + serverTime +
                '}';
    }
}
