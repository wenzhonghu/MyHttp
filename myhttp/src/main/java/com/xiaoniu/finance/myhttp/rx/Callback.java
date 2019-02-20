package com.xiaoniu.finance.myhttp.rx;

/**
 * 结果回调
 * @param <Result>
 */
public interface Callback<Result> {

    void onCompleted();

    void onNext(Result result);

    void onError(Throwable t);

}