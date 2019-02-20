package com.xiaoniu.finance.myhttp.rx;

/**
 * 转化处理器
 * Created by zhonghu on 2019/2/11.
 */

public interface Processor<T, Result> {

    Result process(T t);
}
