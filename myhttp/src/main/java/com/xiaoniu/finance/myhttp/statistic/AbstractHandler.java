package com.xiaoniu.finance.myhttp.statistic;

/**
 * 抽象的处理器
 * 处理消息回调处理器
 * Created by zhonghu on 2019/1/28.
 */
public abstract class AbstractHandler {

    public abstract boolean handleRecvMessage(PushMessage pushMessage);
}
