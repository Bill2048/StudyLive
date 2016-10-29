package com.chaoxing.study;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class LiveActivity extends FragmentActivity {

    private LiveController mLiveController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        mLiveController = (LiveController) findViewById(R.id.live_controller);


        findViewById(R.id.debug_btn_push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLiveController.push();
            }
        });

        findViewById(R.id.debug_btn_pull).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLiveController.pull();
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
        mLiveController.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLiveController.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLiveController.onDestroy();
    }
}
