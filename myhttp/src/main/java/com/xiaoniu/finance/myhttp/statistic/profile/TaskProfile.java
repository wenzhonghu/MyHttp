package com.xiaoniu.finance.myhttp.statistic.profile;

import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.util.StringUtil;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * 请求任务统计信息类
 */

public class TaskProfile implements Cloneable, Comparable<TaskProfile>, Serializable {

    public TaskProfile() {
    }

    public TaskProfile(String taskId, int cmdId) {
        this.taskId = taskId;
        this.cmdId = cmdId;
    }

    /**
     * 任务ID 可以根据任务ID清除指定的正在队列里等待处理的任务
     */
    public String taskId;

    /**
     * 命令编号
     */
    public int cmdId;

    /**
     * 启动时间
     */
    public long startTaskTime;

    /**
     * 结束时间
     */
    public long endTaskTime;

    /**
     * 运行的状态
     */
    public int status;

    /**
     * 优先级
     */
    public int priority;

    /**
     * 任务组的ID 可以根据组ID批量清除正在任务队列的任务
     */
    public String taskGroupID;

    /**
     * 是否取消
     */
    public boolean isCancel;

    /**
     * 完成任务数
     */
    public long completedTaskCount;

    /**
     * 工作缓存池数量
     */
    public int poolSize;

    /**
     * 线程名
     */
    public String threadName;

    /**
     * 工作池最小值
     */
    public int minWorkerSize;

    /**
     * 工作池最大值
     */
    public int maxWorkerSize;

    /**
     * 错误
     */
    public String errCode;


    public static byte[] parseResult(TaskProfile profile) {
        String data = profile.toString();
        return data.getBytes(Charset.forName("UTF-8"));
    }


    public static TaskProfile parseToResult(byte[] data) {
        String str = new String(data, Charset.forName("UTF-8"));
        if (StringUtil.isTextEmpty(str)) {
            return null;
        }
        String[] sps = str.split(Consts.PARAM_D);
        if (sps == null || sps.length <= 0) {
            return null;
        }

        TaskProfile profile = new TaskProfile();
        for (String sp : sps) {
            if (StringUtil.isNotTextEmpty(sp)) {
                String[] ss = sp.split(Consts.PARAM_EQ);
                if (ss != null && ss.length == 2) {
                    if (StringUtil.isValueEmpty(ss[1])) {
                        continue;
                    }
                    if ("taskId".equals(ss[0])) {
                        profile.taskId = ss[1];
                        continue;
                    }
                    if ("cmdId".equals(ss[0])) {
                        profile.cmdId = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("startTaskTime".equals(ss[0])) {
                        profile.startTaskTime = Long.parseLong(ss[1]);
                        continue;
                    }
                    if ("endTaskTime".equals(ss[0])) {
                        profile.endTaskTime = Long.parseLong(ss[1]);
                        continue;
                    }
                    if ("status".equals(ss[0])) {
                        profile.status = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("priority".equals(ss[0])) {
                        profile.priority = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("taskGroupID".equals(ss[0])) {
                        profile.taskGroupID = ss[1];
                        continue;
                    }
                    if ("isCancel".equals(ss[0])) {
                        profile.isCancel = Boolean.parseBoolean(ss[1]);
                        continue;
                    }
                    if ("completedTaskCount".equals(ss[0])) {
                        profile.completedTaskCount = Long.parseLong(ss[1]);
                        continue;
                    }
                    if ("poolSize".equals(ss[0])) {
                        profile.poolSize = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("threadName".equals(ss[0])) {
                        profile.threadName = ss[1];
                        continue;
                    }
                    if ("minWorkerSize".equals(ss[0])) {
                        profile.minWorkerSize = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("maxWorkerSize".equals(ss[0])) {
                        profile.maxWorkerSize = Integer.parseInt(ss[1]);
                        continue;
                    }
                    if ("errCode".equals(ss[0])) {
                        profile.errCode = ss[1];
                        continue;
                    }
                }
            }
        }

        return profile;
    }

    @Override
    public int compareTo(TaskProfile o) {
        return this.taskId.compareTo(o.taskId);
    }


    @Override
    public String toString() {
        return "taskId=" + taskId +
                ",cmdId=" + cmdId +
                ",startTaskTime=" + startTaskTime +
                ",endTaskTime=" + endTaskTime +
                ",status=" + status +
                ",priority=" + priority +
                ",taskGroupID=" + taskGroupID +
                ",isCancel=" + isCancel +
                ",completedTaskCount=" + completedTaskCount +
                ",poolSize=" + poolSize +
                ",threadName=" + threadName +
                ",minWorkerSize=" + minWorkerSize +
                ",maxWorkerSize=" + maxWorkerSize +
                ",errCode=" + errCode;
    }

}
