package com.xiaoniu.finance.myhttp.rx.schedulers;

import com.xiaoniu.finance.myhttp.task.BaseTaskRunnable;

/**
 * 线程调度器
 *
 * Created by zhonghu on 2019/2/11.
 */

public interface Scheduler {

    void schedule(BaseTaskRunnable command);

    void destroy();
}
