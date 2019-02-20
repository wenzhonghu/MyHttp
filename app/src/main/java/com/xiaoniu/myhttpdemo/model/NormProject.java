package com.xiaoniu.myhttpdemo.model;

import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

/**
 */

public class NormProject implements Serializable {

    private static final long serialVersionUID = 1L;

    public static Type getParseType() {
        return new TypeToken<BaseResponse<NormProject>>() {
        }.getType();
    }

    /**
     * 产品ID
     **/
    public long id;

    /**
     * 产品名称
     **/
    public String name;
}
