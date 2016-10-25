package com.chaoxing.study;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

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
        findViewById(R.id.debug_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onclick");
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
