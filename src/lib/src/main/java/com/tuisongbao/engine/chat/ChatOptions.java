package com.tuisongbao.engine.chat;

import com.tuisongbao.engine.common.callback.TSBProgressCallback;
import com.tuisongbao.engine.log.LogUtil;

public class ChatOptions {
    private static final String TAG = ChatOptions.class.getSimpleName();

    private TSBProgressCallback mProgressCallback;
    private int mLastPercent = 0;

    public ChatOptions(TSBProgressCallback callback) {
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
