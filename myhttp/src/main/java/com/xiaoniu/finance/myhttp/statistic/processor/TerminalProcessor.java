package com.xiaoniu.finance.myhttp.statistic.processor;

import android.util.Log;
import com.xiaoniu.finance.myhttp.http.Consts.CmdCode;
import com.xiaoniu.finance.myhttp.statistic.StatisticHandler;
import com.xiaoniu.finance.myhttp.statistic.StatisticProcesser.ProcessorOption;
import com.xiaoniu.finance.myhttp.statistic.profile.BizProfile;
import com.xiaoniu.finance.myhttp.statistic.profile.NetworkProfile;
import com.xiaoniu.finance.myhttp.statistic.profile.TaskProfile;

/**
 * Created by zhonghu on 2019/1/29.
 */

public class TerminalProcessor implements AbstractProcessor {

    private ProcessorOption mOption;

    public TerminalProcessor(ProcessorOption option) {
        this.mOption = option;
    }

    @Override
    public void init() {
    }

    @Override
    public void startup() {
        for (TaskProfile profile : StatisticHandler.getInstance().taskHistory) {
            Log.d(String.valueOf(CmdCode.TASK_CMDID), profile.toString());
        }

        for (NetworkProfile profile : StatisticHandler.getInstance().netHistory) {
            Log.d(String.valueOf(CmdCode.NET_CMDID), profile.toString());
        }

        for (BizProfile profile : StatisticHandler.getInstance().httpHistory) {
            Log.d(String.valueOf(CmdCode.HTTP_CMDID), profile.toString());
        }
    }

    @Override
    public void shutdown() {

    }
}
