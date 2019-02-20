package com.xiaoniu.finance.myhttp.rx.operators;

import com.xiaoniu.finance.myhttp.rx.AsyncJob;
import com.xiaoniu.finance.myhttp.rx.Callback;
import com.xiaoniu.finance.myhttp.rx.Operator;
import com.xiaoniu.finance.myhttp.rx.Processor;
import com.xiaoniu.finance.myhttp.rx.exceptions.Exceptions;

/**
 * 实现filter操作符功能
 * Created by zhonghu on 2019/2/12.
 */

public class FilterOperator<T> implements Operator<T> {

    private AsyncJob<T> source;
    private Processor<T, Boolean> processor;


    public FilterOperator(AsyncJob<T> source, Processor<T, Boolean> processor) {
        this.source = source;
        this.processor = processor;
    }

    @Override
    public void call(Callback<T> callback) {
        FilterCallback<T> mapCallback = new FilterCallback<>(callback, processor);
        source.callback(mapCallback);
    }


    static final class FilterCallback<T> implements Callback<T> {

        private Callback<T> source;
        private Processor<T, Boolean> processor;


        public FilterCallback(Callback<T> source, Processor<T, Boolean> processor) {
            this.source = source;
            this.processor = processor;
        }

        @Override
        public void onCompleted() {
            source.onCompleted();
        }

        @Override
        public void onNext(T t) {
            boolean result;
            try {
                result = processor.process(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                source.onError(e);
                return;
            }
            if (result){
                source.onNext(t);
            }
        }

        @Override
        public void onError(Throwable t) {
            source.onError(t);
        }
    }
}


