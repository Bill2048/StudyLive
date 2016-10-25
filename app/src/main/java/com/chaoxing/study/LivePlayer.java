package com.chaoxing.study;

import android.support.v4.app.FragmentActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

import java.io.IOException;

/**
 * Created by huwei on 2016/10/24.
 */

public class LivePlayer implements View.OnClickListener,
        IMediaPlayer.OnBufferingUpdateListener,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnSeekCompleteListener {

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
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
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
    public void onCompletion(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    public void show() {
        mPlayerWindow.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mPlayerWindow.setVisibility(View.GONE);
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onDestroy() {
    }

}
