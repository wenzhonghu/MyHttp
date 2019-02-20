package com.xiaoniu.myhttpdemo.extra;

import com.xiaoniu.myhttpdemo.extra.event.BaseMessageEvent;

/**
 * ost的实体事件类
 * 定义所需的实体类
 * Created by zhonghu on 2019/1/30.
 */

public class AppMessageEvent extends BaseMessageEvent {
    /**
     * login
     */
    public static class LoginResponseEvent extends ResponseEvent {

    }

    /**
     * test
     */
    public static class TestResponseEvent extends ResponseEvent {

    }
}
