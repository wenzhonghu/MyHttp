package com.xiaoniu.finance.myhttp.rx.schedulers;

import static com.xiaoniu.finance.myhttp.task.TaskExecutorManager.TYPE_HTTP_LOW_PRIORITY;

import com.xiaoniu.finance.myhttp.task.BaseTaskRunnable;
import com.xiaoniu.finance.myhttp.task.TaskExecutorManager;
import com.xiaoniu.finance.myhttp.task.TaskPollExecutor;

/**
 * 守护http工作异步的线程调度器，
 * 优先级很低，可以当着后台守护线程
 */

class WatchScheduler implements Scheduler {

    private static TaskPollExecutor executorService;

    private String taskId;

    @Override
    public void schedule(BaseTaskRunnable command) {
        taskId = command.getTaskGroupdID();
        getWorker().execute(command);
    }

    @Override
    public void destroy() {
        if (executorService != null) {
            executorService.removeTask(taskId);
        }
    }

    private TaskPollExecutor getWorker() {
        synchronized (WatchScheduler.class) {
            if (executorService == null) {
                executorService = TaskExecutorManager.getInstance(TYPE_HTTP_LOW_PRIORITY);
            }
            return executorService;
        }
    }

}
