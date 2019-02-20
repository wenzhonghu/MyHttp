package com.xiaoniu.finance.myhttp.rx.operators;

import com.xiaoniu.finance.myhttp.rx.AsyncJob;
import com.xiaoniu.finance.myhttp.rx.Callback;
import com.xiaoniu.finance.myhttp.rx.Operator;
import com.xiaoniu.finance.myhttp.rx.schedulers.Scheduler;
import com.xiaoniu.finance.myhttp.task.BaseTaskRunnable;

/**
 * 回调线程调度器
 * 仿造rxjava的
 * Created by zhonghu on 2019/2/11.
 */

public class CallbackOnOperator<T> implements Operator<T> {

    private AsyncJob<T> source;
    private Scheduler scheduler;

    public CallbackOnOperator(AsyncJob<T> source, Scheduler scheduler) {
        this.source = source;
        this.scheduler = scheduler;
    }

    @Override
    public void call(Callback<T> callback) {
        CallbackOnCallback<T> callbackOnCallback = new CallbackOnCallback<>(callback, scheduler);
        source.callback(callbackOnCallback);
    }

    static class CallbackOnCallback<Result> implements Callback<Result> {

        private Callback<Result> callback;
        private Scheduler scheduler;

        public CallbackOnCallback(Callback<Result> callback, Scheduler scheduler) {
            this.callback = callback;
            this.scheduler = scheduler;
        }

        @Override
        public void onCompleted() {
            scheduler.schedule(new BaseTaskRunnable(new Runnable() {
                @Override
                public void run() {
                    callback.onCompleted();
                }
            }));
        }

        @Override
        public void onNext(final Result result) {
            scheduler.schedule(new BaseTaskRunnable(new Runnable() {
                @Override
                public void run() {
                    callback.onNext(result);
                }
            }));
        }

        @Override
        public void onError(final Throwable t) {
            scheduler.schedule(new BaseTaskRunnable(new Runnable() {
                @Override
                public void run() {
                    callback.onError(t);
                }
            }));
        }
    }
}
