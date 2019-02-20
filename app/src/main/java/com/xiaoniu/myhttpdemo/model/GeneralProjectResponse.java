package com.xiaoniu.myhttpdemo.model;

import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;


/**
 * xxx
 * Created by wzh on 2018/3/2.
 */
public class GeneralProjectResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public static Type getParseType() {
        return new TypeToken<BaseResponse<GeneralProjectResponse>>() {
        }.getType();
    }

    public ArrayList<NormProject> list;
}
