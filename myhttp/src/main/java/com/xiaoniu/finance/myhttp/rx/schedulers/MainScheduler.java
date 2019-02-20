package com.xiaoniu.finance.myhttp.rx.schedulers;

import android.os.Handler;
import android.os.Looper;
import com.xiaoniu.finance.myhttp.task.BaseTaskRunnable;

/**
 * 主线程调度器
 * Created by zhonghu on 2019/2/11.
 */

class MainScheduler implements Scheduler {

    private static Handler handler;

    @Override
    public void schedule(BaseTaskRunnable command) {
        getWorker().post(command);
    }

    @Override
    public void destroy() {
        //清除 Handler消息队列里的所有消息
        if (handler != null){
            handler.removeCallbacksAndMessages(null);
        }
    }

    private static Handler getWorker() {
        synchronized (MainScheduler.class) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            return handler;
        }
    }
}
