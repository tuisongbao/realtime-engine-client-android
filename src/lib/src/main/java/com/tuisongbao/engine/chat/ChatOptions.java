package com.tuisongbao.engine.chat;

import com.tuisongbao.engine.common.callback.TSBProgressCallback;
import com.tuisongbao.engine.log.LogUtil;

public class ChatOptions {
    private TSBProgressCallback mProgressCallback;
    private int mLastPercent = 0;

    public ChatOptions() {

    }

    public ChatOptions(TSBProgressCallback callback) {
        mProgressCallback = callback;
    }

    public void callbackProgress(int percent) {
        if (mProgressCallback == null) {
            return;
        }
        if (percent > mLastPercent) {
            LogUtil.debug(LogUtil.LOG_TAG_CHAT, "callbackProgress " + percent);
            mProgressCallback.progress(percent);
        }
    }
}
