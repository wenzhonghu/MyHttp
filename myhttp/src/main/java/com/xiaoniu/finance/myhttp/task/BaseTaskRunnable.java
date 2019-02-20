package com.xiaoniu.finance.myhttp.task;

/**
 * 封装Runnable接口
 */
public class BaseTaskRunnable extends AbstractTaskRunnable {

    private Runnable runnable;

    public BaseTaskRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        if (runnable == null) {
            return;
        }
        runnable.run();
    }

}

