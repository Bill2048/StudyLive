package com.chaoxing.study;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class LiveActivity extends FragmentActivity {

    private LiveManager mLiveManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        mLiveManager = new LiveManager(this, findViewById(R.id.live_content));

        findViewById(R.id.debug_btn_push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLiveManager.push();
            }
        });

        findViewById(R.id.debug_btn_pull).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLiveManager.pull();
            }
        });

        findViewById(R.id.iv_launcher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(LiveActivity.this, "Hi~", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLiveManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLiveManager.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLiveManager.onDestroy();
    }
}
