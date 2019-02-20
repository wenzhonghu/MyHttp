package com.xiaoniu.finance.myhttp.statistic;

/**
 * Created by zhonghu on 2019/1/28.
 */

public class PushMessage {
    public PushMessage(int cmdId, byte[] buffer) {
        this.cmdId = cmdId;
        this.buffer = buffer;
    }

    public int cmdId;

    public byte[] buffer;
}
