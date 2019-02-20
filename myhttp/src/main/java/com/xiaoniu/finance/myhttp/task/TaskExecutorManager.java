package com.xiaoniu.finance.myhttp.task;

import java.util.HashMap;

/**
 * 任务线程创建器
 */
public class TaskExecutorManager {

    private final static int EXECUTOR_INDEX_MIN_POOL_SIZE = 0;
    private final static int EXECUTOR_INDEX_MAX_POOL_SIZE = 1;
    private final static int EXECUTOR_INDEX_POOL_RESIZE_DELAY = 2;

    /**
     * 需要快速响应，但是一般不会时间太长的请求 例如：DB的间歇性打开关闭、加载Bitmap、短暂的AIDL（一般最长不超过10秒）
     */
    public static final int TYPE_QUICK = 0;

    /**
     * 正常的请求, 需要正常排队
     */
    public static final int TYPE_NORMAL = 1;

    /**
     * Http请求
     */
    public static final int TYPE_HTTP = 2;

    /**
     * Http请求，优先级和重要程度都很低，不应该占用系统太多资源的类型
     */
    public static final int TYPE_HTTP_LOW_PRIORITY = 3;

    /**
     * 缓存任务线程的各线程参数配置，min，max，delay等
     * 第一维为任务类型；
     * 第二维为任务类型对应的参数配置缓存；
     */
    private final static int[][] EXECUTORS = new int[4][];

    /**
     * 根据任务类型缓存任务线程
     */
    private static HashMap<Integer, TaskPollExecutor> POOL_MANAGER_MAP = new HashMap<Integer, TaskPollExecutor>();

    static {
        EXECUTORS[TYPE_QUICK] = new int[]{3, 10, 10 * 1000};
        EXECUTORS[TYPE_NORMAL] = new int[]{2, 7, 20 * 1000};
        EXECUTORS[TYPE_HTTP] = new int[]{1, 10, 30 * 1000};
        EXECUTORS[TYPE_HTTP_LOW_PRIORITY] = new int[]{0, 1, 5 * 1000};
    }

    private TaskExecutorManager(){
        throw new RuntimeException("single object");
    }

    /**
     *
     * @param type
     * @return
     */
    public synchronized static TaskPollExecutor getInstance(int type) {
        if (type < TYPE_QUICK || type > TYPE_HTTP_LOW_PRIORITY) {
            throw new RuntimeException("不支持该类型的TaskExecutorManager type:" + type);
        }

        TaskPollExecutor executor = POOL_MANAGER_MAP.get(type);
        if (executor != null) {
            return executor;
        }

        int minWorkerSize = EXECUTORS[type][EXECUTOR_INDEX_MIN_POOL_SIZE];
        int maxWorkerSize = EXECUTORS[type][EXECUTOR_INDEX_MAX_POOL_SIZE];
        int resizeWorkerDelay = EXECUTORS[type][EXECUTOR_INDEX_POOL_RESIZE_DELAY];
        executor = new TaskPollExecutor();
        executor.configParams(minWorkerSize, maxWorkerSize, resizeWorkerDelay);
        POOL_MANAGER_MAP.put(type, executor);

        return executor;
    }
}
