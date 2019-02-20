package com.xiaoniu.finance.myhttp.rx.schedulers;

import static com.xiaoniu.finance.myhttp.task.TaskExecutorManager.TYPE_HTTP;

import com.xiaoniu.finance.myhttp.task.BaseTaskRunnable;
import com.xiaoniu.finance.myhttp.task.TaskExecutorManager;
import com.xiaoniu.finance.myhttp.task.TaskPollExecutor;

/**
 * http请求的工作异步线程调度器
 */

class HttpScheduler implements Scheduler {

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
        synchronized (HttpScheduler.class) {
            if (executorService == null) {
                executorService = TaskExecutorManager.getInstance(TYPE_HTTP);
            }
            return executorService;
        }
    }

}
