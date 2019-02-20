package com.xiaoniu.finance.myhttp.rx.operators;

import com.xiaoniu.finance.myhttp.rx.AsyncJob;
import com.xiaoniu.finance.myhttp.rx.Callback;
import com.xiaoniu.finance.myhttp.rx.Operator;

/**
 * 实现flat map操作符功能
 * Created by zhonghu on 2019/2/11.
 */

public class FlatMapOperator<Result> implements Operator<Result> {

    private Operator<AsyncJob<Result>> operator;

    public FlatMapOperator(Operator<AsyncJob<Result>> operator) {
        this.operator = operator;
    }

    @Override
    public void call(Callback<Result> callback) {
        FlatMapCallback<Result> flatMapCallback = new FlatMapCallback<>(callback);
        operator.call(flatMapCallback);
    }

    static class FlatMapCallback<Result> implements Callback<AsyncJob<Result>> {

        private Callback<Result> source;

        public FlatMapCallback(Callback<Result> source) {
            this.source = source;
        }

        @Override
        public void onCompleted() {
            source.onCompleted();
        }

        @Override
        public void onNext(AsyncJob<Result> asyncJob) {
            asyncJob.callback(source);
        }

        @Override
        public void onError(Throwable t) {
            source.onError(t);
        }
    }
}
