package com.chaoxing.study;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by huwei on 2016/10/23.
 */

public class LiveController extends RelativeLayout implements OnPushListener {

    private LiveStreamer mStreamer;
    private boolean mPushing;

    private LivePlayer mPlayer;
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
        mStreamer = new LiveStreamer(getContext(), findViewById(R.id.streamer_window));
        mStreamer.setStreamerListener(this);
        mStreamer.hide();
    }

    private void initPlayer() {
        mPlayer = new LivePlayer(getContext(), findViewById(R.id.player_window));
        mPlayer.hide();
    }

    public void push() {
        mPushing = true;
//        mPlayer.hide();
        mStreamer.resetStreamer();
        mStreamer.show();
        setVisibility(View.VISIBLE);
        if (mStreamer.isInitiated()) {
            mStreamer.startStream();
        } else {
            mStreamer.startCameraPreviewWithPermCheck();
        }
    }

    public void stopPush() {
        mStreamer.stopStream();
        mStreamer.resetStreamer();
    }

    public void pull() {
        mPulling = true;
//        mStreamer.hide();
        mPlayer.resetPlayer();
        mPlayer.show();
        setVisibility(View.VISIBLE);
        mPlayer.prepare("rtmp://chaoxing.rtmplive.ks-cdn.com/live/LIVELI1557281DEC6");
//        mPlayer.prepare("rtmp://chaoxing.rtmplive.ks-cdn.com/live/LIVEWP1559FFCFA92");
    }

    public void stopPull() {
        mPlayer.stopPlayer();
        mPlayer.resetPlayer();
    }

    @Override
    public void onInitiated() {
        if (mPushing) {
            mStreamer.startStream();
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
            mStreamer.onResume();
        }
        if (mPulling) {
            mPlayer.onResume();
        }
    }

    public void onPause() {
        if (mPushing) {
            mStreamer.onPause();
        }
        if (mPulling) {
            mPlayer.onPause();
        }
    }

    public void onDestroy() {
        mStreamer.onDestroy();
        mPlayer.onDestroy();
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }
}
