package com.xiaoniu.myhttpdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

public class LoadingDialogHelper {

    private LoadingDialog mLoadingDialog;

    public void showLoadingDialog(Context context, boolean isCancel, String text) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(context, text);
            mLoadingDialog.setCancelable(isCancel);
        }
        if (!isFinishedState(context) && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    public boolean isShowing() {
        if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
            return false;
        }
        return true;
    }

    private static boolean isFinishedState(Context context) {
        if (context == null) {
            return true;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing()) {
                return true;
            }
            if (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()) {
                return true;
            }
        }
        return false;
    }

}
