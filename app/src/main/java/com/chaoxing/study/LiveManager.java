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
    private boolean mPushing;

    private LivePlayer mLivePlayer;

    public LiveManager(FragmentActivity activity, View liveContent) {
        mActivity = activity;
        mLiveContent = liveContent;
        mDragLayout = (LiveDragLayout) mLiveContent.findViewById(R.id.drag_layout);
        initStreamer();
        initPlayer();
    }

    private void initStreamer() {
        mLiveContent.setVisibility(View.GONE);
        View streamerWindow = mLiveContent.findViewById(R.id.streamer_window);
        mDragLayout.setDragView(streamerWindow);
        mLiveStreamer = new LiveStreamer(mActivity, streamerWindow);
        mLiveStreamer.setStreamerListener(mStreamerListener);
    }

    private void initPlayer() {
        mLivePlayer = new LivePlayer(mActivity, mLiveContent.findViewById(R.id.player_content));
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
        mLiveContent.setVisibility(View.VISIBLE);
        mLiveStreamer.startCameraPreviewWithPermCheck();
    }

    public void onResume() {
        if (mPushing) {
            mLiveStreamer.onResume();
        }
    }

    public void onPause() {
        if (mPushing) {
            mLiveStreamer.onPause();
        }
    }

    public void onDestroy() {
        mLiveStreamer.onDestroy();
    }
}
