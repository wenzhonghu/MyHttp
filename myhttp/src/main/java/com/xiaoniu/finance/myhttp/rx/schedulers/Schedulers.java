package com.xiaoniu.finance.myhttp.rx.schedulers;

/**
 * 总调度器
 * @author zhonghu
 */

public class Schedulers {

    private static class MainSchedulerHolder {

        private static final MainScheduler INSTANCE = new MainScheduler();
    }

    private static class HttpSchedulerHolder {

        private static final HttpScheduler INSTANCE = new HttpScheduler();
    }

    private static class IOSchedulerHolder {

        private static final IOScheduler INSTANCE = new IOScheduler();
    }

    private static class WatchSchedulerHolder {

        private static final WatchScheduler INSTANCE = new WatchScheduler();
    }

    private static class ImmediateSchedulerHolder {

        private static final ImmediateScheduler INSTANCE = new ImmediateScheduler();
    }

    /**
     * 主线程的调度器
     * 常用场景：
     * 回到主线程时的UI操作
     */
    public static Scheduler main() {
        return MainSchedulerHolder.INSTANCE;
    }

    /**
     * http请求的调度器
     * 常用场景：
     * http请求
     */
    public static Scheduler http() {
        return HttpSchedulerHolder.INSTANCE;
    }

    /**
     * io请求的调度器
     * 常用场景：
     * 访问大文件/流
     */
    public static Scheduler io() {
        return IOSchedulerHolder.INSTANCE;
    }


    /**
     * 后台守护者的调度器
     * 常用场景：
     * 执行优先级很低的场景
     */
    public static Scheduler watch() {
        return WatchSchedulerHolder.INSTANCE;
    }

    /**
     * 立即执行的调度器
     * 常用场景：
     * 1/访问sharePrefrence
     * 2/访问assert
     * 3/访问很小的文件/流
     */
    public static Scheduler immediate() {
        return ImmediateSchedulerHolder.INSTANCE;
    }

}
