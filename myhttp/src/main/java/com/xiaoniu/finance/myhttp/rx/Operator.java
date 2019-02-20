package com.xiaoniu.finance.myhttp.rx;

/**
 * 操作符
 * Created by zhonghu on 2019/2/11.
 */

public interface Operator<T> {
    void call(Callback<T> callback);
}
