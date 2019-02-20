package com.xiaoniu.myhttpdemo.extra;

import org.greenrobot.eventbus.EventBus;

/**
 * 使用eventBus的代码
 */
public class EventBusLifePorxy {

    public static final String TAG = EventBusLifePorxy.class.getSimpleName();

    private volatile static EventBusLifePorxy mEventBusLifePorxy;
    public boolean mIsUserEventBus = false;

    private EventBusLifePorxy() {
    }

    public static EventBusLifePorxy getInstance() {
        return new EventBusLifePorxy();
    }

    public void create(Object sub) {
        if (mIsUserEventBus && !EventBus.getDefault().isRegistered(sub)) {
            EventBus.getDefault().register(sub);
        }
    }

    public void destory(Object sub) {
        if (mIsUserEventBus && EventBus.getDefault().isRegistered(sub)) {
            EventBus.getDefault().unregister(sub);
        }
    }

    public void setIfUseEventBus(boolean useEventBus) {
        mIsUserEventBus = useEventBus;
    }
}
