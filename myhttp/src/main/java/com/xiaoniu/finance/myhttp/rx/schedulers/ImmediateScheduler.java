package com.xiaoniu.finance.myhttp.rx.schedulers;

import static com.xiaoniu.finance.myhttp.task.TaskExecutorManager.TYPE_QUICK;

import com.xiaoniu.finance.myhttp.task.BaseTaskRunnable;
import com.xiaoniu.finance.myhttp.task.TaskExecutorManager;
import com.xiaoniu.finance.myhttp.task.TaskPollExecutor;

/**
 * 快速访问工作异步的线程调度器
 */

class ImmediateScheduler implements Scheduler {

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
        synchronized (ImmediateScheduler.class) {
            if (executorService == null) {
                executorService = TaskExecutorManager.getInstance(TYPE_QUICK);
            }
            return executorService;
        }
    }

}
