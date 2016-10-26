package com.chaoxing.study;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by huwei on 2016/10/24.
 */

public class LiveDragLayout extends RelativeLayout {

    private boolean dragEnable;

    private final ViewDragHelper mDragHelper;
    private View mDragView;

    public void setDragView(View dragView) {
        mDragView = dragView;
    }

    public boolean isDragEnable() {
        return dragEnable;
    }

    public void setDragEnable(boolean dragEnable) {
        this.dragEnable = dragEnable;
    }

    public LiveDragLayout(Context context) {
        this(context, null);
    }

    public LiveDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveDragLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
    }

    private boolean mIntercepted;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (dragEnable) {
            final int action = MotionEventCompat.getActionMasked(ev);
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                mDragHelper.cancel();
                mIntercepted = false;
            } else {
                if (action == MotionEvent.ACTION_DOWN) {
                    startX = ev.getX();
                    startY = ev.getY();
                }
                if (startX > mDragView.getLeft() && startX < mDragView.getRight() && startY > mDragView.getTop() && startY < mDragView.getBottom()) {
                    mIntercepted = mDragHelper.shouldInterceptTouchEvent(ev);
                } else {
                    mIntercepted = false;
                }
            }
        } else {
            mIntercepted = super.onInterceptTouchEvent(ev);
        }
        return mIntercepted;
    }

    private float startX = 0, startY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (dragEnable && mIntercepted) {
            mDragHelper.processTouchEvent(ev);
            return true;
        } else {
            return false;
        }
    }

    private final class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragView;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft();
            final int rightBound = getWidth() - mDragView.getWidth();

            final int newLeft = Math.min(Math.max(left, leftBound), rightBound);

            return newLeft;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - mDragView.getHeight();

            final int newTop = Math.min(Math.max(top, topBound), bottomBound);

            return newTop;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return getMeasuredWidth() - child.getMeasuredWidth();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return getMeasuredHeight() - child.getMeasuredHeight();
        }

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

}