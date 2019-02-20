package com.xiaoniu.finance.myhttp.rx.operators;

import com.xiaoniu.finance.myhttp.rx.AsyncJob;
import com.xiaoniu.finance.myhttp.rx.Callback;
import com.xiaoniu.finance.myhttp.rx.Operator;
import com.xiaoniu.finance.myhttp.rx.Processor;
import com.xiaoniu.finance.myhttp.rx.exceptions.Exceptions;

/**
 * 实现map操作符功能
 * Created by zhonghu on 2019/2/11.
 */

public class MapOperator<T, Result> implements Operator<Result> {

    private AsyncJob<T> source;
    private Processor<T, Result> processor;


    public MapOperator(AsyncJob<T> source, Processor<T, Result> processor) {
        this.source = source;
        this.processor = processor;
    }

    @Override
    public void call(Callback<Result> callback) {
        MapCallback<T, Result> mapCallback = new MapCallback<>(callback, processor);
        source.callback(mapCallback);
    }


    static class MapCallback<T, Result> implements Callback<T> {

        private Callback<Result> source;
        private Processor<T, Result> processor;

        public MapCallback(Callback<Result> source, Processor<T, Result> processor) {
            this.source = source;
            this.processor = processor;
        }

        @Override
        public void onCompleted() {
            source.onCompleted();
        }

        @Override
        public void onNext(T t) {
            try {
                Result result = processor.process(t);
                source.onNext(result);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                onError(e);
            }
        }

        @Override
        public void onError(Throwable t) {
            source.onError(t);
        }
    }
}


