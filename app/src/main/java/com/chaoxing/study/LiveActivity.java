package com.chaoxing.study;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

public class LiveActivity extends FragmentActivity {

    private LiveController mLiveController;
    private SwitchCompat mSwPush;
    private SwitchCompat mSwPull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        mLiveController = (LiveController) findViewById(R.id.live_controller);

        mSwPush = (SwitchCompat) findViewById(R.id.sw_push);
        mSwPush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mLiveController.push();
                } else {
                    mLiveController.stopPush();
                }
            }
        });

        mSwPull = (SwitchCompat) findViewById(R.id.sw_pull);

        mSwPull.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mLiveController.pull();
                } else {
                    mLiveController.stopPull();
                }
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
