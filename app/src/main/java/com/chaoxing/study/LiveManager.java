package com.chaoxing.study;

import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by huwei on 2016/10/23.
 */

public class LiveManager {

    FragmentActivity mActivity;
    private View mLiveContent;

    private LiveDragLayout mDragLayout;

    private LiveStreamer mLiveStreamer;
    private View mStreamerWindow;
    private boolean mPushing;

    private LivePlayer mLivePlayer;
    private View mPlayerWindow;
    private boolean mPulling;

    public LiveManager(FragmentActivity activity, View liveContent) {
        mActivity = activity;
        mLiveContent = liveContent;
        mLiveContent.setVisibility(View.GONE);
        mDragLayout = (LiveDragLayout) mLiveContent.findViewById(R.id.drag_layout);
        initStreamer();
        initPlayer();
    }

    private void initStreamer() {
        mStreamerWindow = mLiveContent.findViewById(R.id.streamer_window);
        mLiveStreamer = new LiveStreamer(mActivity, mStreamerWindow);
        mLiveStreamer.setStreamerListener(mStreamerListener);
    }

    private void initPlayer() {
        mPlayerWindow = mLiveContent.findViewById(R.id.player_window);
        mLivePlayer = new LivePlayer(mActivity, mPlayerWindow);
    }

    private StreamerListener mStreamerListener = new StreamerListener() {
        @Override
        public void onInitiated() {
            if (mPushing) {
                mLiveStreamer.startStream();
            }
        }

        @Override
        public void onWindowStyleChanged(LiveStreamer.WindowStyle style) {
            if (LiveStreamer.WindowStyle.SMALL.equals(style)) {
                mDragLayout.setDragEnable(true);
            } else {
                mDragLayout.setDragEnable(false);
            }
        }
    };

    public void push() {
        mPushing = true;
        mLivePlayer.hide();
        mLiveStreamer.show();
        mDragLayout.setDragView(mStreamerWindow);
        mLiveContent.setVisibility(View.VISIBLE);
        mLiveStreamer.startCameraPreviewWithPermCheck();
    }

    public void pull() {
        mPulling = true;
        mLiveStreamer.hide();
        mLivePlayer.show();
        mDragLayout.setDragView(mPlayerWindow);
        mLiveContent.setVisibility(View.VISIBLE);
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
