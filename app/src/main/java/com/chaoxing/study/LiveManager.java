package com.chaoxing.study;

import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by huwei on 2016/10/23.
 */

public class LiveManager implements StreamerListener {

    private FragmentActivity mActivity;
    private View mLiveContent;

    private LiveStreamer mLiveStreamer;
    private boolean mPushing;

    private LivePlayer mLivePlayer;
    private boolean mPulling;

    public LiveManager(FragmentActivity activity, View liveContent) {
        mActivity = activity;
        mLiveContent = liveContent;
        mLiveContent.setVisibility(View.GONE);
        initStreamer();
        initPlayer();
    }

    private void initStreamer() {
        mLiveStreamer = new LiveStreamer(mActivity, mLiveContent.findViewById(R.id.streamer_window));
        mLiveStreamer.setStreamerListener(this);
    }

    private void initPlayer() {
        mLivePlayer = new LivePlayer(mActivity, mLiveContent.findViewById(R.id.player_window));
    }

    public void push() {
        mPushing = true;
        mLivePlayer.hide();
        mLiveStreamer.show();
        mLiveContent.setVisibility(View.VISIBLE);
        mLiveStreamer.startCameraPreviewWithPermCheck();
    }

    public void pull() {
        mPulling = true;
        mLiveStreamer.hide();
        mLivePlayer.show();
        mLiveContent.setVisibility(View.VISIBLE);
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

        }
    }

    public void onPause() {
        if (mPushing) {
            mLiveStreamer.onPause();
        }
        if (mPulling) {

        }
    }

    public void onDestroy() {
        mLiveStreamer.onDestroy();
    }

}
