package com.chaoxing.study;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

import java.io.IOException;

/**
 * Created by huwei on 2016/10/24.
 */

public class LivePlayer implements View.OnClickListener,
        IMediaPlayer.OnBufferingUpdateListener,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnSeekCompleteListener {

    private final static String TAG = LivePlayer.class.getSimpleName();

    private FragmentActivity mActivity;
    private View mPlayerWindow;

    private KSYMediaPlayer mMediaPlayer;

    private View mPlayerContent;
    private SurfaceView mSvPlayer;

    private View mStatusPanel;
    private ProgressBar mPbLoading;
    private Button mBtnStatusOperate;

    private View mControlPanel;
    private View mToolbar;
    private ImageButton mIbtnClose;

    private View mBottomBar;
    private ImageView mIvAnchor;
    private TextView mTvAnchor;
    private TextView mTvTimer;
    private ImageButton mIbtnZoom;

    private int mVideoWidth;
    private int mVideoHeight;

    public LivePlayer(FragmentActivity activity, View playerWindow) {
        mActivity = activity;
        mPlayerWindow = playerWindow;
        initView();
    }

    private void initView() {
        mPlayerContent = mPlayerWindow.findViewById(R.id.player_content);

        mSvPlayer = (SurfaceView) mPlayerWindow.findViewById(R.id.sv_player);

        mStatusPanel = mPlayerWindow.findViewById(R.id.status_panel);
        mPbLoading = (ProgressBar) mPlayerWindow.findViewById(R.id.pb_loading);
        mBtnStatusOperate = (Button) mPlayerWindow.findViewById(R.id.btn_status_operate);
        mBtnStatusOperate.setOnClickListener(this);

        mControlPanel = mPlayerWindow.findViewById(R.id.control_panel);
        mToolbar = mPlayerWindow.findViewById(R.id.toolbar);
        mIbtnClose = (ImageButton) mPlayerWindow.findViewById(R.id.ibtn_close);
        mIbtnClose.setOnClickListener(this);
        mBottomBar = mPlayerWindow.findViewById(R.id.bottom_bar);
        mIvAnchor = (ImageView) mPlayerWindow.findViewById(R.id.iv_anchor);
        mTvAnchor = (TextView) mPlayerWindow.findViewById(R.id.tv_anchor);
        mTvTimer = (TextView) mPlayerWindow.findViewById(R.id.tv_timer);
        mIbtnZoom = (ImageButton) mPlayerWindow.findViewById(R.id.ibtn_zoom);
        mIbtnZoom.setOnClickListener(this);
    }

    private void initPlayer() {
        mMediaPlayer = new KSYMediaPlayer.Builder(mActivity).build();
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.setBufferTimeMax(3);
        mMediaPlayer.setTimeout(5, 30);
        try {
            mMediaPlayer.setDataSource("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_status_operate) {

        } else if (id == R.id.ibtn_close) {

        } else if (id == R.id.ibtn_zoom) {

        }
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mVideoWidth = mMediaPlayer.getVideoWidth();
        mVideoHeight = mMediaPlayer.getVideoHeight();
        mMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        mMediaPlayer.start();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        videoPlayEnd();
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        switch (i) {
            case KSYMediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d(TAG, "Buffering Start.");
                break;
            case KSYMediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d(TAG, "Buffering End.");
                break;
            case KSYMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                Toast.makeText(mActivity, "Audio Rendering Start", Toast.LENGTH_SHORT).show();
                break;
            case KSYMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                Toast.makeText(mActivity, "Video Rendering Start", Toast.LENGTH_SHORT).show();
                break;
            case KSYMediaPlayer.MEDIA_INFO_SUGGEST_RELOAD:
                // Player find a new stream(video or audio), and we could reload the video.
                if (mMediaPlayer != null)
                    mMediaPlayer.reload(mMediaPlayer.getDataSource(), false);
                break;
            case KSYMediaPlayer.MEDIA_INFO_RELOADED:
                Toast.makeText(mActivity, "Succeed to reload video.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Succeed to reload video.");
                return false;
        }
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (width != mVideoWidth || height != mVideoHeight) {
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();

                if (mMediaPlayer != null)
                    mMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            }
        }
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what) {
            case KSYMediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(TAG, "OnErrorListener, Error Unknown:" + what + ",extra:" + extra);
                break;
            default:
                Log.e(TAG, "OnErrorListener, Error:" + what + ",extra:" + extra);
        }

        videoPlayEnd();
        return false;
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        Log.e(TAG, "onSeekComplete()");
    }

    public void show() {
        mPlayerWindow.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mPlayerWindow.setVisibility(View.GONE);
    }

    private void videoPlayEnd() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }

    private boolean mPause;

    public void onResume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mPause = false;
        }
    }

    public void onPause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mPause = true;
        }
    }

    public void onDestroy() {
        mSvPlayer = null;
    }

}
