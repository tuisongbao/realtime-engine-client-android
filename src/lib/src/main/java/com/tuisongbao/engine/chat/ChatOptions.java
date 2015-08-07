package com.tuisongbao.engine.chat;

import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.log.LogUtil;

public class ChatOptions {
    private static final String TAG = "TSB" + ChatOptions.class.getSimpleName();

    private ProgressCallback mProgressCallback;
    private int mLastPercent = 0;

    public ChatOptions(ProgressCallback callback) {
        mProgressCallback = callback;
    }

    public void callbackProgress(int percent) {
        if (mProgressCallback == null) {
            return;
        }
        if (percent > mLastPercent) {
            LogUtil.debug(TAG, "In progress... " + percent);
            mProgressCallback.progress(percent);
        }
    }
}
