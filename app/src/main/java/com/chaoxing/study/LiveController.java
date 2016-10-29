package com.chaoxing.study;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by huwei on 2016/10/23.
 */

public class LiveController extends RelativeLayout implements StreamerListener {

    private LiveStreamer mLiveStreamer;
    private boolean mPushing;

    private LivePlayer mLivePlayer;
    private boolean mPulling;

    public LiveController(Context context) {
        this(context, null);
    }

    public LiveController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_live_content, this);
        initStreamer();
        initPlayer();
    }

    private void initStreamer() {
        mLiveStreamer = new LiveStreamer(getContext(), findViewById(R.id.streamer_window));
        mLiveStreamer.setStreamerListener(this);
        mLiveStreamer.hide();
    }

    private void initPlayer() {
        mLivePlayer = new LivePlayer(getContext(), findViewById(R.id.player_window));
        mLivePlayer.hide();
    }

    public void push() {
        mPushing = true;
        mLivePlayer.hide();
        mLiveStreamer.show();
        setVisibility(View.VISIBLE);
        if (mLiveStreamer.isInitiated()) {
            mLiveStreamer.startStream();
        } else {
            mLiveStreamer.startCameraPreviewWithPermCheck();
        }
    }

    public void pull() {
        mPulling = true;
//        mLiveStreamer.hide();
        mLivePlayer.show();
        setVisibility(View.VISIBLE);
        mLivePlayer.prepare();
    }


    @Override
    public void onInitiated() {
        if (mPushing) {
            mLiveStreamer.startStream();
        }
    }

    @Override
    public void onPushStatusChanged(LiveStreamer.PushStatus status) {
        if (status.equals(LiveStreamer.PushStatus.STOP)) {

        } else if (status.equals(LiveStreamer.PushStatus.PUSHING)) {

        } else if (status.equals(LiveStreamer.PushStatus.PAUSE)) {

        }
    }

    public void onResume() {
        if (mPushing) {
            mLiveStreamer.onResume();
        }
        if (mPulling) {
            mLivePlayer.onResume();
        }
    }

    public void onPause() {
        if (mPushing) {
            mLiveStreamer.onPause();
        }
        if (mPulling) {
            mLivePlayer.onPause();
        }
    }

    public void onDestroy() {
        mLiveStreamer.onDestroy();
        mLivePlayer.onDestroy();
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }
}
