package com.chaoxing.study;

import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by huwei on 2016/10/23.
 */

public class LiveManager {

    FragmentActivity mActivity;
    private View mLiveContent;

    private LiveStreamer mLiveStreamer;
    private boolean mPushing;

    private LivePlayer mLivePlayer;

    public LiveManager(FragmentActivity activity, View liveContent) {
        mActivity = activity;
        mLiveContent = liveContent;
        initStreamer();
        initPlayer();
    }

    private void initStreamer() {
        mLiveContent.setVisibility(View.GONE);
        mLiveStreamer = new LiveStreamer(mActivity, mLiveContent.findViewById(R.id.streamer_content));
        mLiveStreamer.setStreamerListener(mStreamerListener);
    }

    private void initPlayer() {
        mLivePlayer = new LivePlayer(mActivity, mLiveContent.findViewById(R.id.player_content));
    }

    private StreamerListener mStreamerListener = new StreamerListener() {
        @Override
        public void onStreamerInitiated() {
            if (mPushing) {
                mLiveStreamer.startStream();
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
