package com.xiaoniu.finance.myhttp.rx.operators;

import com.xiaoniu.finance.myhttp.rx.Callback;
import com.xiaoniu.finance.myhttp.rx.Operator;
import com.xiaoniu.finance.myhttp.rx.schedulers.Scheduler;
import com.xiaoniu.finance.myhttp.task.BaseTaskRunnable;

/**
 * 工作线程调度器
 * 可以添加多个实现不同线程间的转换调用
 * Created by zhonghu on 2019/2/11.
 */

public class TaskOnOperator<T> implements Operator<T> {

    private Operator<T> operator;
    private Scheduler scheduler;

    public TaskOnOperator(Operator<T> operator, Scheduler scheduler) {
        this.operator = operator;
        this.scheduler = scheduler;
    }

    @Override
    public void call(final Callback<T> callback) {
        BaseTaskRunnable runnable = new BaseTaskRunnable(new Runnable() {
            @Override
            public void run() {
                operator.call(callback);
            }
        });
        if (runnable.isCancel()) {
            scheduler.destroy();
        }
        scheduler.schedule(runnable);
    }
}
