package com.chaoxing.study;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;

/**
 * Created by huwei on 2016/10/23.
 */

public class LiveStreamer implements View.OnClickListener, KSYStreamer.OnInfoListener, KSYStreamer.OnErrorListener, StatsLogReport.OnLogEventListener {

    private final static String TAG = LiveStreamer.class.getSimpleName();

    private Handler mHandler = new Handler();

    private FragmentActivity mActivity;
    private View mContentView;

    private View mStreamerWindow;
    private GLSurfaceView mSvPreviewer;
    private KSYStreamer mStreamer;

    private View mDebugPanel;

    private View mFocusPanel;
    private View mStatusPanel;
    private ProgressBar mPbLoading;
    private TextView mTvStatus;
    private Button mBtnStatusOperate;

    private View mControlPanel;
    private View mToolbar;
    private ImageButton mIbtnClose;
    private TextView mTvTitle;
    private ImageButton mIbtnSwitchCamera;
    private View mBottomBar;
    private TextView mTvAnchor;
    private Chronometer mChTimer;
    private TextView mTvViewerCount;
    private ImageButton mIbtnZoom;

    public enum WindowStyle {
        NORMAL,
        LARGE,
        SMALL,
    }

    private boolean mRecording;
    private boolean mInitiated;

    private StreamerListener streamerListener;

    private GestureDetectorCompat mDetector;

    public LiveStreamer(FragmentActivity activity, View contentView) {
        mActivity = activity;
        mContentView = contentView;
        initDebug();
        initView();
        initWindow();
        initStreamer();
    }

    private void initView() {
        mStreamerWindow = mContentView.findViewById(R.id.streamer_window);
        mSvPreviewer = (GLSurfaceView) mContentView.findViewById(R.id.sv_previewer);

        mFocusPanel = mContentView.findViewById(R.id.focus_panel);
        mFocusPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        });
        mDetector = new GestureDetectorCompat(mActivity, new MoveWindow());

        mStatusPanel = mContentView.findViewById(R.id.status_panel);
        mStatusPanel.setVisibility(View.GONE);
        mPbLoading = (ProgressBar) mContentView.findViewById(R.id.pb_loading);
        mTvStatus = (TextView) mContentView.findViewById(R.id.tv_status);
        mBtnStatusOperate = (Button) mContentView.findViewById(R.id.btn_status_operate);

        mControlPanel = mContentView.findViewById(R.id.control_panel);
        mToolbar = mContentView.findViewById(R.id.toolbar);
        mIbtnClose = (ImageButton) mContentView.findViewById(R.id.ibtn_close);
        mIbtnClose.setOnClickListener(this);
        mTvTitle = (TextView) mContentView.findViewById(R.id.tv_title);
        mIbtnSwitchCamera = (ImageButton) mContentView.findViewById(R.id.ibtn_switch_camera);
        mIbtnSwitchCamera.setOnClickListener(this);
        mBottomBar = mContentView.findViewById(R.id.bottom_bar);
        mTvAnchor = (TextView) mContentView.findViewById(R.id.tv_anchor);
        mChTimer = (Chronometer) mContentView.findViewById(R.id.ch_timer);
        mTvViewerCount = (TextView) mContentView.findViewById(R.id.tv_viewer_count);
        mIbtnZoom = (ImageButton) mContentView.findViewById(R.id.ibtn_zoom);
        mIbtnZoom.setOnClickListener(this);
    }

    private int land;

    private void initDebug() {
        mDebugPanel = mContentView.findViewById(R.id.debug_panel);
        if (BuildConfig.DEBUG) {
            mDebugPanel.setVisibility(View.VISIBLE);
        }
    }

    private void initWindow() {
        ViewGroup.LayoutParams lp = mStreamerWindow.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = 640;
        mStreamerWindow.setLayoutParams(lp);
        ViewGroup.LayoutParams lpSv = mSvPreviewer.getLayoutParams();
        lpSv.width = 480;
        lpSv.height = 640;
        mSvPreviewer.setLayoutParams(lpSv);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ibtn_close) {
            stopStream();
            mStreamer.stopCameraPreview();
        } else if (id == R.id.ibtn_switch_camera) {
            switchCamera();
        } else if (id == R.id.ibtn_zoom) {
            zoomWindow();
        }
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            if (mWindowStyle.equals(WindowStyle.SMALL)) {
//                zoomWindow();
//            } else {
//                toggleControlPanel();
//            }
//        }
//        return false;
//    }

    private Runnable mHideControlPanelRunnable = new Runnable() {
        @Override
        public void run() {
            mControlPanel.setVisibility(View.GONE);
        }
    };

    private void toggleControlPanel() {
        if (mControlPanel.getVisibility() == View.VISIBLE) {
            toggleControlPanel(false);
        } else {
            toggleControlPanel(true);
        }
    }

    private void toggleControlPanel(boolean toggle) {
        mHandler.removeCallbacks(mHideControlPanelRunnable);
        if (toggle) {
            mHandler.postDelayed(mHideControlPanelRunnable, 5000);
            mControlPanel.setVisibility(View.VISIBLE);
        } else {
            mControlPanel.setVisibility(View.GONE);
        }
    }

    private void initStreamer() {
        // 创建KSYStreamer实例
        mStreamer = new KSYStreamer(mActivity);
        // 设置预览View
        mStreamer.setDisplayPreview(mSvPreviewer);
        // 设置推流url（需要向相关人员申请，测试地址并不稳定！）
        mStreamer.setUrl("rtmp://chaoxing.uplive.ks-cdn.com/live/LIVEWP1559FFCFA92?vdoid=1477167372");
        // 设置预览分辨率, 当一边为0时，SDK会根据另一边及实际预览View的尺寸进行计算
        mStreamer.setPreviewResolution(480, 0);
        // 设置推流分辨率，可以不同于预览分辨率
        mStreamer.setTargetResolution(480, 0);
        // 设置预览帧率
        mStreamer.setPreviewFps(15);
        // 设置推流帧率，当预览帧率大于推流帧率时，编码模块会自动丢帧以适应设定的推流帧率
        mStreamer.setTargetFps(15);
        // 设置视频码率，分别为初始平均码率、最高平均码率、最低平均码率，单位为kbps，另有setVideoBitrate接口，单位为bps
        mStreamer.setVideoKBitrate(400, 600, 200);
        mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(), ImgTexFilterMgt.KSY_FILTER_BEAUTY_DISABLE);
        // 设置音频采样率
        mStreamer.setAudioSampleRate(44100);
        // 设置音频码率，单位为kbps，另有setAudioBitrate接口，单位为bps
        mStreamer.setAudioKBitrate(48);
        // 开启前置摄像头
        mStreamer.setFrontCameraMirror(true);
        // 设置编码模式(软编、硬编):
        // StreamerConstants.ENCODE_METHOD_SOFTWARE
        // StreamerConstants.ENCODE_METHOD_HARDWARE
        mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
        // 设置屏幕的旋转角度，支持 0, 90, 180, 270
        mStreamer.setRotateDegrees(mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 0 : 90);
        // 开启推流统计功能
        mStreamer.setEnableStreamStatModule(true);
        mStreamer.setOnInfoListener(this);
        mStreamer.setOnErrorListener(this);
        mStreamer.setOnLogEventListener(this);
    }

    @Override
    public void onInfo(int what, int msg1, int msg2) {
        switch (what) {
            case StreamerConstants.KSY_STREAMER_CAMERA_INIT_DONE:
                Log.d(TAG, "推流初始化完成");
                mInitiated = true;
                streamerListener.onStreamerInitiated();
//                if (mAutoStart) {
//                    startStream();
//                }
                break;
            case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                Log.d(TAG, "推流成功");
                mChTimer.setBase(SystemClock.elapsedRealtime());
                mChTimer.start();
                break;
            case StreamerConstants.KSY_STREAMER_FRAME_SEND_SLOW:
                Log.d(TAG, "网络状态不佳，当前帧发送时长：" + msg1 + "ms");
                break;
            case StreamerConstants.KSY_STREAMER_EST_BW_RAISE:
                Log.d(TAG, "码率上调：" + msg1 / 1000 + "kbps");
                break;
            case StreamerConstants.KSY_STREAMER_EST_BW_DROP:
                Log.d(TAG, "码率下调：" + msg1 / 1000 + "kpbs");
                break;
            default:
                Log.d(TAG, "OnInfo() what：" + what + " msg1：" + msg1 + " msg2：" + msg2);
                break;
        }
    }

    @Override
    public void onError(int what, int msg1, int msg2) {
        switch (what) {
            case StreamerConstants.KSY_STREAMER_ERROR_DNS_PARSE_FAILED:
                Log.d(TAG, "URL域名解析失败");
                break;
            case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_FAILED:
                Log.d(TAG, "网络连接失败");
                break;
            case StreamerConstants.KSY_STREAMER_ERROR_PUBLISH_FAILED:
                Log.d(TAG, "向服务器推流失败");
                break;
            case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_BREAKED:
                Log.d(TAG, "网络连接断开");
                break;
            case StreamerConstants.KSY_STREAMER_ERROR_AV_ASYNC:
                Log.d(TAG, "音频采集PTS差值超过：" + msg1 + "ms");
                break;
            case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                Log.d(TAG, "视频编码器初始化失败");
                break;
            case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                Log.d(TAG, "视频编码失败");
                break;
            case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED:
                Log.d(TAG, "音频编码初始化失败");
                break;
            case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN:
                Log.d(TAG, "音频编码失败");
                break;
            case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                Log.d(TAG, "摄像头未知错误");
                break;
            case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                Log.d(TAG, "打开摄像头失败");
                break;
            case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                Log.d(TAG, "系统Camera服务进程退出");
                break;
            case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                Log.d(TAG, "录音开启失败");
                break;
            case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                Log.d(TAG, "录音开启未知错误");
                break;
            default:
                Log.d(TAG, "onError() what：" + what + " msg1：" + msg1 + " msg2：" + msg2);
                break;
        }
        switch (what) {
            case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:  // 摄像头未知错误
            case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:  // 打开摄像头失败
            case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:  // 录音开启失败
            case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:  // 录音开启未知错误
                Toast.makeText(mActivity, "[" + what + "] " + "推流失败", Toast.LENGTH_SHORT).show();
                stopStream();
                break;
            case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:  // 系统Camera服务进程退出
                mStreamer.stopCameraPreview();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCameraPreviewWithPermCheck();
                    }
                }, 5000);
                break;
            default:
                stopStream();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startStream();
                    }
                }, 3000);
                break;
        }
    }

    @Override
    public void onLogEvent(StringBuilder stringBuilder) {
        Log.i("**" + TAG, "onLogEvent() ：" + stringBuilder.toString());
    }

    public void stopLive() {
//        mChronometer.stop();
//        mRecording = false;
    }


    public void startStream() {
        mStreamer.startStream();
        mRecording = true;
    }

    public void stopStream() {
        mStreamer.stopStream();
        mChTimer.stop();
        mRecording = false;
    }

    private void switchCamera() {
        mStreamer.switchCamera();
    }

    public void startCameraPreviewWithPermCheck() {
        mStreamer.startCameraPreview();
    }

    public boolean isInitiated() {
        return mInitiated;
    }

    public void show() {
        mContentView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mContentView.setVisibility(View.GONE);
    }

    private WindowStyle mWindowStyle = WindowStyle.NORMAL;

    private void zoomWindow() {
        if (mWindowStyle.equals(WindowStyle.LARGE)) {
            setWindowStyle(WindowStyle.SMALL);
        } else if (mWindowStyle.equals(WindowStyle.SMALL)) {
            setWindowStyle(WindowStyle.NORMAL);
        } else {
            setWindowStyle(WindowStyle.LARGE);
        }
    }

    public void setWindowStyle(WindowStyle style) {
        mWindowStyle = style;
        if (style.equals(WindowStyle.LARGE)) {
            ViewGroup.MarginLayoutParams lpWindow = (ViewGroup.MarginLayoutParams) mStreamerWindow.getLayoutParams();
            lpWindow.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lpWindow.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lpWindow.leftMargin = 0;
            lpWindow.topMargin = 0;
            lpWindow.rightMargin = 0;
            lpWindow.bottomMargin = 0;
            mStreamerWindow.setLayoutParams(lpWindow);
            mStreamerWindow.invalidate();

            Rect appRect = new Rect();
            mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(appRect);
            int delta = Math.min(appRect.width() / 3, appRect.height() / 4);
            ViewGroup.LayoutParams lpSv = mSvPreviewer.getLayoutParams();
            lpSv.width = delta * 3;
            lpSv.height = delta * 4;
            mSvPreviewer.setLayoutParams(lpSv);

            toggleControlPanel(true);
        } else if (style.equals(WindowStyle.SMALL)) {
            ViewGroup.MarginLayoutParams lpWindow = (ViewGroup.MarginLayoutParams) mStreamerWindow.getLayoutParams();
            lpWindow.width = 300;
            lpWindow.height = 400;
            lpWindow.leftMargin = 0;
            lpWindow.topMargin = dp2px(mActivity, 50);
            lpWindow.rightMargin = 0;
            lpWindow.bottomMargin = 0;
            mStreamerWindow.setLayoutParams(lpWindow);

            ViewGroup.LayoutParams lpSv = mSvPreviewer.getLayoutParams();
            lpSv.width = 300;
            lpSv.height = 400;
            mSvPreviewer.setLayoutParams(lpSv);

            toggleControlPanel(false);
        } else {
            ViewGroup.MarginLayoutParams lpWindow = (ViewGroup.MarginLayoutParams) mStreamerWindow.getLayoutParams();
            lpWindow.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lpWindow.height = 640;
            lpWindow.leftMargin = 0;
            lpWindow.topMargin = 0;
            lpWindow.rightMargin = 0;
            lpWindow.bottomMargin = 0;
            mStreamerWindow.setLayoutParams(lpWindow);

            ViewGroup.LayoutParams lpSv = mSvPreviewer.getLayoutParams();
            lpSv.width = 480;
            lpSv.height = 640;
            mSvPreviewer.setLayoutParams(lpSv);

            toggleControlPanel(true);
        }
    }

    public void onResume() {
        startCameraPreviewWithPermCheck();
        mStreamer.onResume();
    }

    public void onPause() {
        mStreamer.onPause();
        mStreamer.stopCameraPreview();
    }

    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        mStreamer.release();
    }

    public void setStreamerListener(StreamerListener streamerListener) {
        this.streamerListener = streamerListener;
    }

    private int dp2px(Context context, int dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    private final class MoveWindow extends GestureDetector.SimpleOnGestureListener {

        private String TAG = "GestureDetector";

        private float mLeftMargin;
        private float mTopMargin;
        private float mRightMargin;
        private float mBottomMargin;

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown()");
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mStreamerWindow.getLayoutParams();
            mLeftMargin = mlp.leftMargin;
            mTopMargin = mlp.topMargin;
            mRightMargin = mlp.rightMargin;
            mBottomMargin = mlp.bottomMargin;
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp()");
            if (mWindowStyle.equals(WindowStyle.SMALL)) {
                zoomWindow();
            } else {
                toggleControlPanel();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll() x : " + (e1.getRawX() - e2.getRawX()) + " y : " + (e1.getRawY() - e2.getRawY()));

            return true;
        }


    }

}
