package com.xiaoniu.finance.myhttp.task;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import com.xiaoniu.finance.myhttp.Global;
import com.xiaoniu.finance.myhttp.http.Consts.CmdCode;
import com.xiaoniu.finance.myhttp.statistic.PushMessage;
import com.xiaoniu.finance.myhttp.statistic.StatisticHandler;
import com.xiaoniu.finance.myhttp.statistic.profile.TaskProfile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


/**
 * [任务池-线程池简化修改优化版] 线程池的简化实现，功能：<br/>
 * 1、线程池
 * 2、根据设置的MinWorkerSize和MaxWorkerSize对线程池的线程进行线程维持
 * 例如min=0,max=5的时候，如果指定时间内没有任务执行则将线程数量减少到0，整个执行器将会停止所有工作，当任务数大于5的时候最多开启5个线程进行任务处理
 * <p/>
 * 当将Runnable 替换为 AbstractTaskRunnable 的时候将获得以下功能：
 * 1、默认任务队列带优先级，任务可按优先级进行处理，AbstractTaskRunnable中的priority越低则优先级越高
 * 2、可按ID对任务进行清除
 */
public class TaskPollExecutor {

    private final static String TAG = TaskPollExecutor.class.getSimpleName();

    private final static String TN = "XN-Task-Thread-";

    private BlockingQueue<Runnable> workQueue;

    private final HashSet<Worker> workers = new HashSet<Worker>();

    private final ReentrantLock mainLock = new ReentrantLock();

    static final int RUNNING = 0;
    static final int SHUTDOWN = 1;
    static final int STOP = 2;
    static final int TERMINATED = 3;

    volatile int runState = RUNNING;

    private long completedTaskCount;
    private volatile int poolSize;
    private int threadNameIndex;

    private int minWorkerSize = 3;
    private int maxWorkerSize = 10;
    private long resizeWorkerDelay = 10 * 1000L;

    @SuppressLint("Executors")
    private ScheduledExecutorService resizeWorkerTimer = Executors.newSingleThreadScheduledExecutor();
    private ResizeTimer resizeTimerTask;

    /**
     * 带优先级的无界阻塞队列，每次出队都返回优先级最高的元素
     */
    public TaskPollExecutor() {
        this(new PriorityBlockingQueue<Runnable>());
    }

    public TaskPollExecutor(BlockingQueue queue) {
        this.workQueue = queue;
    }

    /**
     * 执行任务（本质添加队列中）
     */
    public void execute(Runnable runnable) {
        if (runState != RUNNING) {
            return;
        }
        if (!(runnable instanceof AbstractTaskRunnable)) {
            runnable = new BaseTaskRunnable(runnable);
        }
        if (poolSize < maxWorkerSize) {
            addIfUnderCorePoolSize(runnable);
            return;
        }
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            workQueue.offer(runnable);
            if (getTotalCount() > minWorkerSize) {
                cancelResizeWorker();
            }
        } finally {
            mainLock.unlock();
        }
    }

    public void configParams(int minWorkerSize, int maxWorkerSize, long resizeWorkerDelay) {
        this.minWorkerSize = minWorkerSize;
        this.maxWorkerSize = maxWorkerSize;
        this.resizeWorkerDelay = resizeWorkerDelay;
    }

    public void removeTask(String taskID) {
        if (taskID == null) {
            return;
        }
        removeTask(false, taskID);
    }

    public void removeAllTask() {
        removeTask(true, null);
    }

    /**
     * 循环取消任务组
     */
    private void removeTask(boolean isCancelAll, String taskGroupdID) {
        mainLock.lock();
        try {
            removeWorkerTask(isCancelAll, taskGroupdID);
            Iterator<Runnable> it = workQueue.iterator();
            while (it.hasNext()) {
                Runnable runnable = it.next();
                if (!(runnable instanceof AbstractTaskRunnable)) {
                    continue;
                }
                AbstractTaskRunnable task = (AbstractTaskRunnable) runnable;
                if (isCancelAll) {
                    task.cancel();
                    it.remove();
                    continue;
                }
                if (task.getTaskGroupdID() == null) {
                    continue;
                }
                if (!taskGroupdID.equals(task.getTaskGroupdID())) {
                    continue;
                }
                task.cancel();
                it.remove();
            }
        } finally {
            mainLock.unlock();
        }

    }

    /**
     * 循环取消线程组
     *
     * @param isCancelAll 则不关心groupID也要cancel
     * @param taskGroupID 相等时则cancel
     */
    private void removeWorkerTask(boolean isCancelAll, String taskGroupID) {
        Iterator<Worker> it = workers.iterator();
        while (it.hasNext()) {
            Worker worker = it.next();
            Runnable runnable = worker.getRecordCurrentTask();
            if (runnable == null) {
                continue;
            }
            if (!(runnable instanceof AbstractTaskRunnable)) {
                continue;
            }
            AbstractTaskRunnable task = (AbstractTaskRunnable) runnable;
            if (isCancelAll) {
                task.cancel();
                worker.clearCurrentRunTask();
                continue;
            }
            if (task.getTaskGroupdID() == null) {
                continue;
            }
            if (!taskGroupID.equals(task.getTaskGroupdID())) {
                continue;
            }
            task.cancel();
            worker.clearCurrentRunTask();
        }
    }

    /**
     * 释放线程池和任务队列
     */
    public void release() {
        mainLock.lock();
        try {
            workQueue.clear();
            Iterator<Worker> it = workers.iterator();

            while (it.hasNext()) {
                Worker worker = it.next();
                boolean isInterrupted = worker.interruptIfIdle();
                if (!isInterrupted) {
                    continue;
                }
                it.remove();
                poolSize--;
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 同步给线程池添加一个线程
     */
    private boolean addIfUnderCorePoolSize(Runnable firstTask) {
        Thread t = null;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (poolSize < maxWorkerSize && runState == RUNNING)
            //考虑增加线程池个数
            {
                t = addThread(firstTask);
            }
        } finally {
            mainLock.unlock();
        }
        return t != null;
    }

    /**
     * 增加真实线程任务worker并且启动缓存到线程池中
     */
    private Thread addThread(Runnable firstTask) {
        Worker w = new Worker(firstTask);
        Thread t = new Thread(w);
        t.setName(TN + (threadNameIndex++));
        boolean workerStarted = false;
        w.thread = t;
        workers.add(w);
        poolSize++;
        try {
            t.start();
            workerStarted = true;
        } finally {
            if (!workerStarted) {
                workers.remove(w);
            }
        }
        return t;
    }

    private Runnable getTask() {
        try {
            Runnable task = workQueue.take();
            if (task == null) {
                return null;
            }
            return task;
        } catch (InterruptedException e) {
        }
        return null;
    }

    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    /**
     * 整个任务数
     * 已完成任务数+正执行任务数+队列中待执行的任务数
     */
    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
                if (w.isActive()) {
                    ++n;
                }
            }
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 已完成的任务数
     */
    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
            }
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 正在运行的任务数
     */
    public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers) {
                if (w.isActive()) {
                    ++n;
                }
            }
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    public int getTotalCount() {
        int activeCount = getActiveCount();
        int queueSize = workQueue.size();
        // Log.v(TAG, "getTotalCount activeCount:" + activeCount +
        // " - queueSize:" + queueSize + " - total:" + (activeCount +
        // queueSize));
        return activeCount + queueSize;
    }

    /**
     * 工作任务
     */
    private final class Worker implements Runnable {

        private final ReentrantLock runLock = new ReentrantLock();

        private Runnable firstTask;

        private Runnable currentRunTask;

        volatile long completedTasks;

        Thread thread;

        volatile boolean hasRun = false;

        Worker(Runnable firstTask) {
            this.firstTask = firstTask;
        }

        /**
         * 根据锁定状态来判断任务是否正在执行中
         */
        boolean isActive() {
            return runLock.isLocked();
        }

        /**
         * 当执行完当前任务后再关闭线程
         */
        boolean interruptIfIdle() {
            final ReentrantLock runLock = this.runLock;
            if (runLock.tryLock()) {
                try {
                    if (hasRun && thread != Thread.currentThread()) {
                        thread.interrupt();
                    }
                    return true;
                } finally {
                    runLock.unlock();
                }
            }
            return false;
        }

        /**
         * 立刻关闭线程
         */
        void interruptNow() {
            if (hasRun) {
                thread.interrupt();
            }
        }

        Runnable getRecordCurrentTask() {
            return currentRunTask;
        }

        void clearCurrentRunTask() {
            currentRunTask = null;
        }


        /**
         * 执行任务
         */
        private void runTask(Runnable task) {
            long startTaskTime = SystemClock.currentThreadTimeMillis();
            final ReentrantLock runLock = this.runLock;
            String errCode = null;
            runLock.lock();
            try {
                currentRunTask = task;
                /**无效的线程则强制中断*/
                if ((runState >= STOP || (Thread.interrupted() && runState >= STOP))
                        && hasRun) {
                    thread.interrupt();
                }
                boolean ran = false;
                /**执行前的回调*/
                beforeExecute(thread, task);
                try {
                    /**执行*/
                    task.run();
                    ran = true;
                    /**执行完的回调*/
                    afterExecute(task, null);
                    ++completedTasks;
                } catch (RuntimeException ex) {
                    if (!ran) {
                        afterExecute(task, ex);
                    }
                    errCode = ex.getLocalizedMessage();
                    throw ex;
                }
            } finally {
                runLock.unlock();
                try {
                    statistic((AbstractTaskRunnable) task, startTaskTime, errCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void statistic(AbstractTaskRunnable task, long startTaskTime, String errCode) {
            if (Global.enableStatistic()) {
                AbstractTaskRunnable t = task;
                String tid = t != null ? t.getTaskID() : UUID.randomUUID().toString();
                String groupId = t != null ? t.getTaskGroupdID() : "";
                int p = t != null ? t.getPriority() : 0;
                boolean cancel = t != null ? t.isCancel() : false;

                TaskProfile profile = new TaskProfile(tid, CmdCode.TASK_CMDID);
                profile.priority = p;
                profile.taskGroupID = groupId;
                profile.startTaskTime = startTaskTime;
                profile.endTaskTime = SystemClock.currentThreadTimeMillis();
                profile.status = runState;
                profile.isCancel = cancel;
                profile.completedTaskCount = getCompletedTaskCount();
                profile.poolSize = poolSize;
                profile.minWorkerSize = minWorkerSize;
                profile.maxWorkerSize = maxWorkerSize;
                profile.threadName = TN + threadNameIndex;
                profile.errCode = errCode;
                PushMessage msg = new PushMessage(CmdCode.TASK_CMDID, TaskProfile.parseResult(profile));
                StatisticHandler.getInstance().handleRecvMessage(msg);
            }
        }

        @Override
        public void run() {
            try {
                hasRun = true;
                Runnable task = firstTask;
                firstTask = null;
                while (task != null || (task = getTask()) != null) {
                    runTask(task);
                    task = null;
                }
            } finally {
                workerDone(this);
            }
        }
    }


    /**
     * 执行前的回调
     */
    protected void beforeExecute(Thread t, Runnable r) {
    }

    /**
     * 执行后的回调
     */
    protected void afterExecute(Runnable r, Throwable t) {
        mainLock.lock();
        try {
            int queueSize = workQueue.size();
            if (queueSize > minWorkerSize) {
                return;
            }
            int totalCount = getTotalCount() - 1;
            if (totalCount > minWorkerSize) {
                return;
            }

            /**不足任务队列时，则准备启动延时清空线程*/
            if (resizeTimerTask == null) {
                resizeTimerTask = new ResizeTimer();
                resizeWorkerTimer.schedule(resizeTimerTask, resizeWorkerDelay, TimeUnit.MILLISECONDS);
            }
        } finally {
            mainLock.unlock();
        }
    }


    /**
     * 工作线程完成后
     */
    void workerDone(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks;
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }
    }


    /**
     *
     */
    class ResizeTimer extends TimerTask {

        @Override
        public void run() {
            int queueSize = workQueue.size();
            if (queueSize > minWorkerSize) {
                return;
            }
            resizeWorker();
            resizeTimerTask = null;
        }
    }

    /**
     * 取消 "重设任务器"
     */
    private void cancelResizeWorker() {
        ResizeTimer resizeTimerTask = this.resizeTimerTask;
        if (resizeTimerTask == null) {
            return;
        }
        resizeTimerTask.cancel();
        this.resizeTimerTask = null;
    }

    /**
     * 重设无效的工作线程
     */
    private void resizeWorker() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            /**
             * 队列还有空间则无需清除
             */
            int queueSize = getTotalCount();
            if (queueSize >= minWorkerSize && minWorkerSize != 0) {
                return;
            }

            /**
             * 无Worker则无需清除
             */
            int removeWorkCount = workers.size() - minWorkerSize;
            if (removeWorkCount <= 0) {
                return;
            }

            /**
             * 移除已中断的Worker
             */
            int removeFlag = 0;
            Iterator<Worker> it = workers.iterator();
            while (it.hasNext()) {
                Worker worker = it.next();
                boolean isInterrupted = worker.interruptIfIdle();
                if (!isInterrupted) {
                    continue;
                }
                it.remove();
                removeFlag++;
                poolSize--;
                if (removeWorkCount == removeFlag) {
                    break;
                }
            }
        } finally {
            mainLock.unlock();
        }

    }

}
