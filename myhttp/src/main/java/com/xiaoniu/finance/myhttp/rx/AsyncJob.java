package com.xiaoniu.finance.myhttp.rx;

import com.xiaoniu.finance.myhttp.rx.operators.CallbackOnOperator;
import com.xiaoniu.finance.myhttp.rx.operators.FilterOperator;
import com.xiaoniu.finance.myhttp.rx.operators.FlatMapOperator;
import com.xiaoniu.finance.myhttp.rx.operators.MapOperator;
import com.xiaoniu.finance.myhttp.rx.operators.TaskOnOperator;
import com.xiaoniu.finance.myhttp.rx.schedulers.Scheduler;

/**
 * 实现类似rxjava库，提供基本rx功能
 * 1/提供create和from两个静态方法创建rx对象
 * 2/提供map操作符实现映射功能
 * 3/提供flatMap操作符flat映射功能
 * 4/提供filter操作符过滤功能
 * 5/其他操作符仿造rxjava代码改造即可
 *
 * <p/>
 * 为啥不直接引用rxjava库而自己仿造实现一套rx机制？
 * 主要原因是：为了实现对外统一提供一套线程池操作而编写的特定异步访问方案。
 *
 * <p/>
 * 另外：
 * 1/rxjava体系太大引入没必要
 * 2/本库只需要根据自身业务，自身业务不会出现没有响应式编程中的按压式等问题
 * 3/按需实现缩减版响应式编程
 *
 * @author zhonghu
 */

public class AsyncJob<T> {

    private Operator<T> dataOperator;

    private AsyncJob(Operator<T> operator) {
        this.dataOperator = operator;
    }

    public static <T> AsyncJob<T> create(Operator<T> operator) {
        return new AsyncJob<>(operator);
    }

    public static <T> AsyncJob<T> from(final T... datas) {
        return new AsyncJob<>(new Operator<T>() {
            @Override
            public void call(Callback<T> callback) {
                for (T data : datas) {
                    callback.onNext(data);
                }
            }
        });
    }

    /**
     * 实现回调操作
     */
    public final void callback(Callback<T> callback) {
        dataOperator.call(callback);
    }

    /**
     * 实现filter操作
     */
    public final AsyncJob<T> fliter(Processor<T, Boolean> processor) {
        return new AsyncJob<>(new FilterOperator<>(this, processor));
    }

    /**
     * 实现map操作
     */
    public final <Result> AsyncJob<Result> map(Processor<T, Result> processor) {
        return new AsyncJob<>(new MapOperator<>(this, processor));
    }

    /**
     * 实现flat map操作
     */
    public final <Result> AsyncJob<Result> flatMap(Processor<T, AsyncJob<Result>> processor) {
        AsyncJob<AsyncJob<Result>> flat = map(processor);
        return new AsyncJob<>(new FlatMapOperator<>(flat.dataOperator));
    }

    /**
     * 实现工作线程调度器
     */
    public final AsyncJob<T> taskOn(Scheduler scheduler) {
        return new AsyncJob<>(new TaskOnOperator<>(dataOperator, scheduler));
    }

    /**
     * 实现回调线程调度器
     */
    public final AsyncJob<T> callbackOn(Scheduler scheduler) {
        return new AsyncJob<>(new CallbackOnOperator<>(this, scheduler));
    }
}
