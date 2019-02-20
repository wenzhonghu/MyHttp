package com.xiaoniu.myhttpdemo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class LoadingDialog extends ProgressDialog {

    private TextView mTextView;
    private String mMessage = null;

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
    }

    public LoadingDialog(Context context) {
        super(context);
    }

    public LoadingDialog(Context context, String msg) {
        super(context);
        this.mMessage = msg;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_wait_dialog);
        setScreenBrightness();
        mTextView = (TextView) LoadingDialog.this
                .findViewById(R.id.loading_text);
        if (mMessage != null) {
            mTextView.setText(mMessage);
        }
    }

    private void setScreenBrightness() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0;
        window.setAttributes(lp);

    }

}
