package com.xiaoniu.myhttpdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.myhttpdemo.api.DemoApi;
import com.xiaoniu.myhttpdemo.extra.AppMessageEvent;
import com.xiaoniu.myhttpdemo.extra.OnInnerRequestListener;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private boolean isRequesting = false;

    private Button mBtn;



    public static void startMe(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mBtn = findViewById(R.id.btn);
        mBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestData(true);
            }
        });
    }






    private void requestData(final boolean isShowLoading) {
        if (isRequesting) {
            return;
        }
        if (isFinishing()) {
            return;
        }
        if (isShowLoading) {
        }
        isRequesting = true;
        //request api
        DemoApi.requestLogin( TAG, new OnInnerRequestListener(new AppMessageEvent.LoginResponseEvent()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void processLogin(AppMessageEvent.LoginResponseEvent responseEvent) {
        isRequesting = false;
        Object result = responseEvent.result;
        Request request = responseEvent.request;
        int state = responseEvent.state;
    }
}
