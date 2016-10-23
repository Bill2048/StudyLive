package com.chaoxing.study;

import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by huwei on 2016/10/24.
 */

public class LivePlayer {

    private FragmentActivity mActivity;
    private View mContentView;

    public LivePlayer(FragmentActivity activity, View contentView) {
        mActivity = activity;
        mContentView = contentView;

    }

    public void show() {
        mContentView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mContentView.setVisibility(View.GONE);
    }
}
