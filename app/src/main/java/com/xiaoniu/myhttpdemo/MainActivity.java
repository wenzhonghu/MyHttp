package com.xiaoniu.myhttpdemo;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends BaseActivity {


    private static final String TAG = "MainActivity";

    private Button tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.sample_text);

        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Main2Activity.startMe(MainActivity.this);
            }
        });
    }


}
