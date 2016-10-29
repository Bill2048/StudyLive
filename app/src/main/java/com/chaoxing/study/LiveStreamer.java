package com.chaoxing.study;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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

    private Context mContext;
    private View mStreamerWindow;

    private LiveDragLayout mDragLayout;
    private View mStreamerContent;
    private GLSurfaceView mSvPreviewer;
    private KSYStreamer mStreamer;

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

    public enum PushStatus {
        STOP,
        PUSHING,
        PAUSE,
    }

    private boolean mRecording;
    private boolean mInitiated;

    private StreamerListener streamerListener;

    private boolean mAnimating;

    public LiveStreamer(Context context, View streamerWindow) {
        mContext = context;
        mStreamerWindow = streamerWindow;
        initView();
        initStreamer();
    }

    private void initView() {
        mDragLayout = (LiveDragLayout) mStreamerWindow.findViewById(R.id.drag_layout);

        mStreamerContent = mStreamerWindow.findViewById(R.id.streamer_content);
        mDragLayout.setDragView(mStreamerContent);

        mSvPreviewer = (GLSurfaceView) mStreamerWindow.findViewById(R.id.sv_previewer);

        mFocusPanel = mStreamerWindow.findViewById(R.id.focus_panel);
        mFocusPanel.setOnClickListener(this);

        mStatusPanel = mStreamerWindow.findViewById(R.id.status_panel);
        mStatusPanel.setVisibility(View.GONE);
        mPbLoading = (ProgressBar) mStreamerWindow.findViewById(R.id.pb_loading);
        mTvStatus = (TextView) mStreamerWindow.findViewById(R.id.tv_status);
        mBtnStatusOperate = (Button) mStreamerWindow.findViewById(R.id.btn_status_operate);

        mControlPanel = mStreamerWindow.findViewById(R.id.control_panel);
        mToolbar = mStreamerWindow.findViewById(R.id.toolbar);
        mIbtnClose = (ImageButton) mStreamerWindow.findViewById(R.id.ibtn_close);
        mIbtnClose.setOnClickListener(this);
        mTvTitle = (TextView) mStreamerWindow.findViewById(R.id.tv_title);
        mIbtnSwitchCamera = (ImageButton) mStreamerWindow.findViewById(R.id.ibtn_switch_camera);
        mIbtnSwitchCamera.setOnClickListener(this);
        mBottomBar = mStreamerWindow.findViewById(R.id.bottom_bar);
        mTvAnchor = (TextView) mStreamerWindow.findViewById(R.id.tv_anchor);
        mChTimer = (Chronometer) mStreamerWindow.findViewById(R.id.ch_timer);
        mTvViewerCount = (TextView) mStreamerWindow.findViewById(R.id.tv_viewer_count);
        mIbtnZoom = (ImageButton) mStreamerWindow.findViewById(R.id.ibtn_zoom);
        mIbtnZoom.setOnClickListener(this);
        setWindowStyle(WindowStyle.NORMAL);
    }

    private long mLastClick;

    @Override
    public void onClick(View view) {
        if (System.currentTimeMillis() - mLastClick <= 500) {
            return;
        }
        mLastClick = System.currentTimeMillis();
        int id = view.getId();
        if (id == R.id.ibtn_close) {
            stopStream();
        } else if (id == R.id.ibtn_switch_camera) {
            switchCamera();
        } else if (id == R.id.ibtn_zoom) {
            zoomWindow();
        } else if (id == R.id.focus_panel) {
            if (!mAnimating) {
                if (mWindowStyle.equals(WindowStyle.SMALL)) {
                    zoomWindow();
                } else {
                    toggleControlPanel();
                }
            }
        }
    }

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
        toggleControlPanel(toggle, true);
    }

    private void toggleControlPanel(boolean toggle, boolean animation) {
        mHandler.removeCallbacks(mHideControlPanelRunnable);
        final int visible;
        float alpha;
        if (toggle) {
            mHandler.postDelayed(mHideControlPanelRunnable, 5000);
            visible = View.VISIBLE;
            alpha = 0f;
        } else {
            visible = View.GONE;
            alpha = 1f;
        }
        if ((toggle && mControlPanel.getVisibility() == View.VISIBLE) || (!toggle && mControlPanel.getVisibility() == View.GONE)) {
            return;
        }
        if (animation) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha == 0f ? 1f : 0f);
            alphaAnimation.setDuration(300);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mControlPanel.setVisibility(visible);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mControlPanel.startAnimation(alphaAnimation);
        } else {
            mControlPanel.setVisibility(visible);
        }
    }

    private void initStreamer() {
        // 创建KSYStreamer实例
        mStreamer = new KSYStreamer(mContext);
        // 设置预览View
        mStreamer.setDisplayPreview(mSvPreviewer);
        // 设置推流url（需要向相关人员申请，测试地址并不稳定！）
//        mStreamer.setUrl("rtmp://chaoxing.uplive.ks-cdn.com/live/LIVEWP1559FFCFA92?vdoid=1477167372");
        mStreamer.setUrl("rtmp://chaoxing.uplive.ks-cdn.com/live/LIVELI1557281DEC6?vdoid=1477499329");
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
        mStreamer.setRotateDegrees(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 0 : 90);
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
                streamerListener.onInitiated();
//                if (mAutoStart) {
//                    startStream();
//                }
                break;
            case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                Log.d(TAG, "推流成功");
//                startCameraPreviewWithPermCheck();
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
                Toast.makeText(mContext, "[" + what + "] " + "推流失败", Toast.LENGTH_SHORT).show();
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
        startCameraPreviewWithPermCheck();
        mStreamer.startStream();
        mRecording = true;
    }

    public void stopStream() {
        mStreamer.stopCameraPreview();
        mStreamer.stopStream();
        mChTimer.stop();
        mRecording = false;
        streamerListener.onPushStatusChanged(PushStatus.PUSHING);
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
        mStreamerWindow.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mStreamerWindow.setVisibility(View.GONE);
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
        if (style.equals(WindowStyle.LARGE)) {
            ValueAnimator animator = ValueAnimator.ofFloat(mStreamerContent.getHeight(), mStreamerWindow.getHeight());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    float scale = value / mStreamerContent.getHeight();

                    mStreamerContent.setScaleY(scale);
                    mStreamerContent.setTranslationY((value - mStreamerContent.getHeight()) / 2);

                    mSvPreviewer.setScaleX(scale);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    toggleControlPanel(false, false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ViewGroup.LayoutParams lpContent = mStreamerContent.getLayoutParams();
                    lpContent.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lpContent.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    mStreamerContent.setLayoutParams(lpContent);

                    int delta = Math.min(mStreamerWindow.getWidth() / 3, mStreamerWindow.getHeight() / 4);
                    ViewGroup.LayoutParams lpSv = mSvPreviewer.getLayoutParams();
                    lpSv.width = delta * 3;
                    lpSv.height = delta * 4;
                    mSvPreviewer.setLayoutParams(lpSv);

                    mDragLayout.setDragEnable(false);

                    mStreamerContent.setScaleY(1);
                    mStreamerContent.setTranslationY(0);

                    mSvPreviewer.setScaleX(1);

                    toggleControlPanel(true, false);

                    mAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.setDuration(300);
            animator.setTarget(mStreamerContent);
            animator.start();
        } else if (style.equals(WindowStyle.SMALL)) {
            ValueAnimator animator = ValueAnimator.ofFloat(mStreamerContent.getHeight(), 400f);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    float scale = value / mStreamerContent.getHeight();

                    mStreamerContent.setScaleX(scale);
                    mStreamerContent.setScaleY(scale);
                    mStreamerContent.setTranslationX(-(scale * mStreamerContent.getWidth() - mStreamerContent.getWidth()) / 2);
                    mStreamerContent.setTranslationY((value - mStreamerContent.getHeight()) / 2);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mAnimating = true;
                    toggleControlPanel(false, false);
                    mDragLayout.setDragEnable(true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    float scale = 400f / mStreamerContent.getHeight();
                    ViewGroup.MarginLayoutParams lpContent = (ViewGroup.MarginLayoutParams) mStreamerContent.getLayoutParams();
                    lpContent.width = (int) (mStreamerContent.getWidth() * scale);
                    lpContent.height = 400;
                    mStreamerContent.setLayoutParams(lpContent);
                    mStreamerContent.setScaleX(1);
                    mStreamerContent.setScaleY(1);
                    mStreamerContent.setTranslationX(0);
                    mStreamerContent.setTranslationY(0);

                    ViewGroup.LayoutParams lpSv = mSvPreviewer.getLayoutParams();
                    lpSv.width = 300;
                    lpSv.height = 400;
                    mSvPreviewer.setLayoutParams(lpSv);

                    mAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.setDuration(300);
            animator.setTarget(mStreamerContent);
            animator.start();
        } else {
            ViewGroup.MarginLayoutParams lpContent = (ViewGroup.MarginLayoutParams) mStreamerContent.getLayoutParams();
            lpContent.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lpContent.height = 640;
            mStreamerContent.setLayoutParams(lpContent);

            ViewGroup.LayoutParams lpSv = mSvPreviewer.getLayoutParams();
            lpSv.width = 480;
            lpSv.height = 640;
            mSvPreviewer.setLayoutParams(lpSv);

            toggleControlPanel(true, false);
            mDragLayout.setDragEnable(false);
        }
        mWindowStyle = style;
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

}
