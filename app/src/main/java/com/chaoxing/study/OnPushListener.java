package com.chaoxing.study;

/**
 * Created by huwei on 2016/10/24.
 */

public interface OnPushListener {

    void onInitiated();

    void onPushStatusChanged(LiveStreamer.PushStatus status);

}
