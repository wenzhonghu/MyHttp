package com.xiaoniu.myhttpdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.xiaoniu.myhttpdemo.extra.EventBusLifePorxy;

/**
 * Created by zhonghu on 2019/1/30.
 */

public class BaseActivity extends AppCompatActivity {

    private EventBusLifePorxy mEventBusLifePorxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setIfUseEventBus(isUseEventBus());
        super.onCreate(null);
        if (mEventBusLifePorxy != null) {
            mEventBusLifePorxy.create(this);
        }
    }

    private void setIfUseEventBus(boolean useEventBus) {
        if (mEventBusLifePorxy == null) {
            mEventBusLifePorxy = EventBusLifePorxy.getInstance();
        }
        mEventBusLifePorxy.setIfUseEventBus(useEventBus);
    }


    protected boolean isUseEventBus() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEventBusLifePorxy == null){
            return;
        }
        mEventBusLifePorxy.destory(this);
    }
}
