package com.chaoxing.study;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

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

    private Handler mHandler = new Handler();

    public final int[] PREVIEW_SIZE_NORMAL = new int[]{480, 640};
    public final int[] PREVIEW_SIZE_SMALL = new int[]{360, 480};

    public enum WindowStyle {
        NORMAL,
        LARGE,
        SMALL,
    }

    private Context mContext;
    private View mPlayerWindow;

    private KSYMediaPlayer mMediaPlayer;

    private LiveDragLayout mDragLayout;
    private View mPlayerContent;
    private SurfaceView mSvPlayer;
    private SurfaceHolder mSvHolder;

    private View mFocusPanel;

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

    private WindowStyle mWindowStyle = WindowStyle.NORMAL;
    private int mVideoWidth;
    private int mVideoHeight;

    private boolean mStopped;
    private boolean mPause;
    private boolean mAnimating;

    public LivePlayer(Context context, View playerWindow) {
        mContext = context;
        mPlayerWindow = playerWindow;
        initView();
        setWindowStyle(WindowStyle.NORMAL);
        initPlayer();
    }

    private void initView() {
        mDragLayout = (LiveDragLayout) mPlayerWindow.findViewById(R.id.drag_layout);

        mPlayerContent = mPlayerWindow.findViewById(R.id.player_content);
        mDragLayout.setDragView(mPlayerContent);

        mSvPlayer = (SurfaceView) mPlayerWindow.findViewById(R.id.sv_player);
        mSvHolder = mSvPlayer.getHolder();
        mSvHolder.addCallback(mSurfaceCallback);

        mFocusPanel = mPlayerWindow.findViewById(R.id.focus_panel);
        mFocusPanel.setOnClickListener(this);

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
        mMediaPlayer = new KSYMediaPlayer.Builder(mContext).build();
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
    }

    public void resetPlayer() {
        mStopped = false;
        mPause = false;
        setWindowStyle(WindowStyle.NORMAL);
    }

    public void prepare(String dataSource) {
        try {
            if (dataSource != null && dataSource.trim().length() != 0) {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(dataSource);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setDisplay(mSvHolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlayer() {
        if (mStopped) {
            return;
        }
        mStopped = true;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    private long mLastClick;

    @Override
    public void onClick(View v) {
        if (System.currentTimeMillis() - mLastClick <= 400) {
            return;
        }
        mLastClick = System.currentTimeMillis();
        int id = v.getId();
        if (id == R.id.btn_status_operate) {

        } else if (id == R.id.ibtn_close) {

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

    private final SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSvHolder = holder;
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(holder);
                mMediaPlayer.setScreenOnWhilePlaying(true);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // 此处非常重要，必须调用!!!
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(null);
            }
        }
    };


    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        Log.d(TAG, "onPrepared()");
        mVideoWidth = mMediaPlayer.getVideoWidth();
        mVideoHeight = mMediaPlayer.getVideoHeight();
        mMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        mMediaPlayer.start();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        Log.d(TAG, "onCompletion()");
//        videoPlayEnd();
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
                Toast.makeText(mContext, "Audio Rendering Start", Toast.LENGTH_SHORT).show();
                break;
            case KSYMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                Toast.makeText(mContext, "Video Rendering Start", Toast.LENGTH_SHORT).show();
                break;
            case KSYMediaPlayer.MEDIA_INFO_SUGGEST_RELOAD:
                // Player find a new stream(video or audio), and we could reload the video.
                if (mMediaPlayer != null)
                    mMediaPlayer.reload(mMediaPlayer.getDataSource(), false);
                break;
            case KSYMediaPlayer.MEDIA_INFO_RELOADED:
                Toast.makeText(mContext, "Succeed to reload video.", Toast.LENGTH_SHORT).show();
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

                if (mMediaPlayer != null) {
                    mMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                }
            }
        }
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        switch (what) {
            case KSYMediaPlayer.MEDIA_ERROR_IO:
                Log.e(TAG, "网络连接超时" + what + ",extra:" + extra);
                break;
            case KSYMediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(TAG, "OnErrorListener, Error Unknown:" + what + ",extra:" + extra);
                break;
            default:
                Log.e(TAG, "OnErrorListener, Error:" + what + ",extra:" + extra);
        }

//        videoPlayEnd();
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

    public void onResume() {
        if (mMediaPlayer != null && !mStopped) {
            mMediaPlayer.start();
            mPause = false;
        }
    }

    public void onPause() {
        if (mMediaPlayer != null && !mStopped) {
            mMediaPlayer.pause();
            mPause = true;
        }
    }

    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mSvPlayer = null;
        mHandler = null;
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

    private void zoomWindow() {
        if (mWindowStyle.equals(WindowStyle.NORMAL)) {
            setWindowStyle(WindowStyle.LARGE);
        } else if (mWindowStyle.equals(WindowStyle.LARGE)) {
            setWindowStyle(WindowStyle.SMALL);
        } else {
            setWindowStyle(WindowStyle.NORMAL);
        }
    }

    public void setWindowStyle(WindowStyle style) {
        if (style.equals(WindowStyle.LARGE)) {
            zoomLargeWindow();
        } else if (style.equals(WindowStyle.SMALL)) {
            zoomSmallWindow();
        } else {
            zoomNormalWindow();
        }
    }

    private void zoomNormalWindow() {
        final float maxScaleX = ((float) mPlayerWindow.getWidth()) / mPlayerContent.getWidth();
        final float maxScaleY = ((float) PREVIEW_SIZE_NORMAL[1]) / mPlayerContent.getHeight();
        ValueAnimator animator = ValueAnimator.ofInt(mPlayerContent.getWidth(), mPlayerWindow.getWidth());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                float scale = ((float) value) / mPlayerContent.getWidth();

                float scaleX = Math.min(scale, maxScaleX);
                float scaleY = Math.min(scale, maxScaleY);
                mPlayerContent.setScaleX(scaleX);
                mPlayerContent.setScaleY(scaleY);
                mPlayerContent.setTranslationX(-(mPlayerContent.getWidth() * scaleX - mPlayerContent.getWidth()) / 2);
                mPlayerContent.setTranslationY((mPlayerContent.getHeight() * scaleY - mPlayerContent.getHeight()) / 2);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
                mDragLayout.setDragEnable(false);
                toggleControlPanel(false, false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mWindowStyle = WindowStyle.NORMAL;

                ViewGroup.LayoutParams lpContent = mPlayerContent.getLayoutParams();
                lpContent.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lpContent.height = PREVIEW_SIZE_NORMAL[1];
                mPlayerContent.setLayoutParams(lpContent);

                mPlayerContent.setScaleX(1);
                mPlayerContent.setScaleY(1);
                mPlayerContent.setTranslationX(0);
                mPlayerContent.setTranslationY(0);

                ViewGroup.LayoutParams lpSv = mSvPlayer.getLayoutParams();
                lpSv.width = PREVIEW_SIZE_NORMAL[0];
                lpSv.height = PREVIEW_SIZE_NORMAL[1];
                mSvPlayer.setLayoutParams(lpSv);
                mSvPlayer.getHolder().setFixedSize(PREVIEW_SIZE_NORMAL[0], PREVIEW_SIZE_NORMAL[1]);

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
        animator.setTarget(mPlayerContent);
        animator.start();
    }

    private void zoomLargeWindow() {
        ValueAnimator animator = ValueAnimator.ofFloat(mPlayerContent.getHeight(), mPlayerWindow.getHeight());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float scale = value / mPlayerContent.getHeight();

                mPlayerContent.setScaleY(scale);
                mPlayerContent.setTranslationY((value - mPlayerContent.getHeight()) / 2);

                mSvPlayer.setScaleX(scale);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mDragLayout.setDragEnable(false);
                toggleControlPanel(false, false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mWindowStyle = WindowStyle.LARGE;

                ViewGroup.LayoutParams lpContent = mPlayerContent.getLayoutParams();
                lpContent.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lpContent.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mPlayerContent.setLayoutParams(lpContent);

                mPlayerContent.setScaleY(1);
                mPlayerContent.setTranslationY(0);

                int delta = Math.min(mPlayerWindow.getWidth() / 3, mPlayerWindow.getHeight() / 4);
                ViewGroup.LayoutParams lpSv = mSvPlayer.getLayoutParams();
                lpSv.width = delta * 3;
                lpSv.height = delta * 4;
                mSvPlayer.setLayoutParams(lpSv);

                mSvPlayer.setScaleX(1);

                mDragLayout.setDragEnable(false);
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
        animator.setTarget(mPlayerContent);
        animator.start();
    }

    private void zoomSmallWindow() {
        ValueAnimator animator = ValueAnimator.ofFloat(mPlayerContent.getHeight(), PREVIEW_SIZE_SMALL[1]);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float scale = value / mPlayerContent.getHeight();

                mPlayerContent.setScaleX(scale);
                mPlayerContent.setScaleY(scale);
                mPlayerContent.setTranslationX(-(scale * mPlayerContent.getWidth() - mPlayerContent.getWidth()) / 2);
                mPlayerContent.setTranslationY((value - mPlayerContent.getHeight()) / 2);
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
                mWindowStyle = WindowStyle.SMALL;

                float scale = ((float) PREVIEW_SIZE_SMALL[1]) / mPlayerContent.getHeight();
                ViewGroup.MarginLayoutParams lpContent = (ViewGroup.MarginLayoutParams) mPlayerContent.getLayoutParams();
                lpContent.width = (int) (mPlayerContent.getWidth() * scale);
                lpContent.height = PREVIEW_SIZE_SMALL[1];
                mPlayerContent.setLayoutParams(lpContent);

                mPlayerContent.setScaleX(1);
                mPlayerContent.setScaleY(1);
                mPlayerContent.setTranslationX(0);
                mPlayerContent.setTranslationY(0);

                ViewGroup.LayoutParams lpSv = mSvPlayer.getLayoutParams();
                lpSv.width = PREVIEW_SIZE_SMALL[0];
                lpSv.height = PREVIEW_SIZE_SMALL[1];
                mSvPlayer.setLayoutParams(lpSv);

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
        animator.setTarget(mPlayerContent);
        animator.start();
    }

}
