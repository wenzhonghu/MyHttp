package com.xiaoniu.myhttpdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.xiaoniu.finance.myhttp.http.Consts;
import com.xiaoniu.finance.myhttp.http.request.Request;
import com.xiaoniu.finance.myhttp.rx.AsyncJob;
import com.xiaoniu.finance.myhttp.rx.Callback;
import com.xiaoniu.finance.myhttp.rx.Processor;
import com.xiaoniu.finance.myhttp.rx.schedulers.Schedulers;
import com.xiaoniu.myhttpdemo.api.DemoApi;
import com.xiaoniu.myhttpdemo.client.Constants;
import com.xiaoniu.myhttpdemo.extra.AppMessageEvent;
import com.xiaoniu.myhttpdemo.extra.OnInnerRequestListener;
import com.xiaoniu.myhttpdemo.model.BaseResponse;
import com.xiaoniu.myhttpdemo.model.GeneralProjectResponse;
import com.xiaoniu.myhttpdemo.model.NormProject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class Main2Activity extends BaseActivity implements OnClickListener {


    private static final String TAG = "Main2Activity";

    private boolean isRequesting = false;
    private TextView tv;
    private ProgressBar progressBar;
    private LoadingDialogHelper mLoadingDialogHelper;
    private Button btn;
    private GeneralProjectResponse data;

    public static void startMe(Context context) {
        Intent intent = new Intent(context, Main2Activity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        tv = findViewById(R.id.sample_text);
        btn = findViewById(R.id.async);
        progressBar = findViewById(R.id.progressBar);
        mLoadingDialogHelper = new LoadingDialogHelper();

        btn.setOnClickListener(this);
        requestData(true);
    }

    @Override
    protected boolean isUseEventBus() {
        return true;
    }


    static int cor = 1;

    private void requestData(final boolean isShowLoading) {
        Log.e("cor", cor + "");
        cor++;
        if (isRequesting) {
            return;
        }
        if (isFinishing()) {
            return;
        }
        if (isShowLoading) {
            mLoadingDialogHelper.showLoadingDialog(this, false, "加载中。。。");
        }
        isRequesting = true;
        //request api
        DemoApi.requestTest("YXN", TAG, new OnInnerRequestListener(new AppMessageEvent.TestResponseEvent()));
    }

    static int co = 1;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void processTest(AppMessageEvent.TestResponseEvent responseEvent) {
        Log.e("co", co + "");
        co++;
        isRequesting = false;
        mLoadingDialogHelper.dismissLoadingDialog();
        Object result = responseEvent.result;
        Request request = responseEvent.request;
        int state = responseEvent.state;

        if (request.isCancelReqesut()) {
            return;
        }
        String errorTip = getResponeErrorTip(state, result, true);
        if (!TextUtils.isEmpty(errorTip)) {
            Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
            return;
        }
        BaseResponse response = (BaseResponse) result;
        data = (GeneralProjectResponse) response.data;
        if (data != null && data.list != null) {

            tv.setText(data.list.get(0).name);

        }
    }


    private String getResponeErrorTip(int state, Object result, boolean isNeedData) {
        if (state == Consts.HttpRespCode.STATE_NO_NETWORK) {
            return this.getString(R.string.err_net_tip);
        }
        if (result == null) {
            return this.getString(R.string.err_loading_tip);
        }
        BaseResponse response = (BaseResponse) result;
        if (!response.isSuccess()) {
            if (Constants.ResultCode.AUTO_TOKEN_ERRO.equals(response.code)) {
                // token 失效会有弹出框处理
                return this.getString(R.string.token_invalid_tip);
            }
            if (TextUtils.isEmpty(response.message)) {
                return this.getString(R.string.err_loading_tip);
            }
            return response.message;
        }
        if (isNeedData && response.data == null) {
            return this.getString(R.string.err_loading_tip);
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        testAsyncJob2("test.txt");
        //testAsyncJob(data.list);
    }

    private void testAsyncJob2(final String assetFile) {
        AsyncJob.from(assetFile).map(new Processor<String, InputStream>() {
            @Override
            public InputStream process(String s) {
                Log.e("w", Thread.currentThread().getName());
                InputStream is = null;
                AssetManager am = getAssets();
                try {
                    is = am.open(assetFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return is;
            }
        })
                .taskOn(Schedulers.immediate())
                .fliter(new Processor<InputStream, Boolean>() {
                    @Override
                    public Boolean process(InputStream inputStream) {
                        Log.e("w", Thread.currentThread().getName());
                        return inputStream != null;
                    }
                }).map(new Processor<InputStream, String>() {
            @Override
            public String process(InputStream inputStream) {
                Log.e("w", Thread.currentThread().getName());
                StringBuilder sb = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String str;
                    while ((str = br.readLine()) != null) {
                        sb.append(str);
                    }
                    br.close();
                } catch (Exception e) {

                }
                return sb.toString();
            }
        }).taskOn(Schedulers.io()).callbackOn(Schedulers.main())
                .callback(new Callback<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.e("w", Thread.currentThread().getName());
                        tv.setText(s);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }
                });

    }


    private void testAsyncJob(ArrayList<NormProject> list) {
        AsyncJob.from(list).fliter(new Processor<ArrayList<NormProject>, Boolean>() {
            @Override
            public Boolean process(ArrayList<NormProject> normProjects) {
                return !normProjects.isEmpty();
            }
        }).flatMap(new Processor<ArrayList<NormProject>, AsyncJob<NormProject>>() {
            @Override
            public AsyncJob<NormProject> process(ArrayList<NormProject> normProjects) {
                return AsyncJob.from(normProjects.get(0));
            }
        })
                //.taskOn(Schedulers.immediate()).callbackOn(Schedulers.main())
                .callback(new Callback<NormProject>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(NormProject normProject) {

                        tv.setText(normProject.name);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }
                });
    }
}
