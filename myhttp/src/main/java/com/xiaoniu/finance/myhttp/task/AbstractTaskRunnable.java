package com.xiaoniu.finance.myhttp.task;

/**
 * [线程任务类]<br/>
 *
 * priority越小，优先级高，priority相同，后入栈的先执行
 *
 */
public abstract class AbstractTaskRunnable implements Comparable<AbstractTaskRunnable>, Runnable {

    /**
     * 优先级
     */
    private int priority;

    /**
     * 任务ID 可以根据任务ID清除指定的正在队列里等待处理的任务
     */
    private String taskID;

    /**
     * 任务组的ID 可以根据组ID批量清除正在任务队列的任务
     */
    private String taskGroupdID;

    /**
     * 是否取消
     */
    private boolean isCancel;

    @Override
    public int compareTo(AbstractTaskRunnable o) {
        return this.priority > o.priority ? 1 : this.priority < o.priority ? -1 : 0;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTaskGroupdID() {
        return taskGroupdID;
    }

    public void setTaskGroupdID(String taskGroupdID) {
        this.taskGroupdID = taskGroupdID;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    void cancel() {
        this.isCancel = true;
        onCancel();
    }

    public void onCancel() {
    }

    public boolean isCancel() {
        return this.isCancel;
    }
}
