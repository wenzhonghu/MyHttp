package com.xiaoniu.finance.myhttp.util;

import android.app.Activity;
import android.content.Context;

/**
 * 对Ui中的Handler.Post的Runnbale进行封装
 * 判断相应的界面是否已经关闭，关闭则不执行Runnable
 */
public abstract class PostUiRunnable implements Runnable {

    private Context mContext;

    public PostUiRunnable(Context context) {
        this.mContext = context;
    }

    @Override
    public void run() {
        if (mContext == null) {
            return;
        }
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            if (activity.isFinishing()) {
                return;
            }
            if (android.os.Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()) {
                return;
            }
        }
        postRun();
    }

    /**
     * run
     */
    public abstract void postRun();
}
