package com.xiaoniu.finance.myhttp.statistic;

import android.util.Log;
import com.xiaoniu.finance.myhttp.Global;
import com.xiaoniu.finance.myhttp.http.Consts.CmdCode;
import com.xiaoniu.finance.myhttp.statistic.profile.BizProfile;
import com.xiaoniu.finance.myhttp.statistic.profile.NetworkProfile;
import com.xiaoniu.finance.myhttp.statistic.profile.TaskProfile;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 统计处理器
 * Created by zhonghu on 2019/1/28.
 */

public class StatisticHandler extends AbstractHandler {

    public static String TAG = StatisticHandler.class.getSimpleName();

    private final static StatisticHandler sInstance = new StatisticHandler();

    private StatisticHandler() {
    }

    public static StatisticHandler getInstance() {
        return sInstance;
    }


    /**
     * 获取任务处理器的统计数据
     */
    public static volatile LinkedBlockingDeque<TaskProfile> taskHistory = new LinkedBlockingDeque<>();

    /**
     * 获取信号的统计数据
     */
    public static volatile LinkedBlockingDeque<NetworkProfile> netHistory = new LinkedBlockingDeque<>();


    /**
     * 统计网络请求的过程数据
     */
    public static volatile LinkedBlockingDeque<BizProfile> httpHistory = new LinkedBlockingDeque<>();


    @Override
    public boolean handleRecvMessage(PushMessage pushMessage) {
        try {
            switch (pushMessage.cmdId) {
                case CmdCode.TASK_CMDID: {
                    TaskProfile profile = TaskProfile.parseToResult(pushMessage.buffer);
                    if (profile != null) {
                        if (Global.isDebug())Log.d(String.valueOf(pushMessage.cmdId), profile.toString());
                        taskHistory.add(profile);
                    }
                }
                return true;
                case CmdCode.NET_CMDID: {
                    NetworkProfile profile = NetworkProfile.parseToResult(pushMessage.buffer);
                    if (profile != null) {
                        if (Global.isDebug())Log.d(String.valueOf(pushMessage.cmdId), profile.toString());
                        netHistory.add(profile);
                    }
                }
                return true;
                case CmdCode.FLOW_CMDID: {
                }
                return true;
                case CmdCode.HTTP_CMDID: {
                    BizProfile profile = BizProfile.parseToResult(pushMessage.buffer);
                    if (profile != null) {
                        if (Global.isDebug())Log.d(String.valueOf(pushMessage.cmdId), profile.toString());
                        httpHistory.add(profile);
                    }
                }
                return true;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "handle pushMessage failed, %s", e);
        }

        return false;
    }
}