package com.tuisongbao.android.engine.chat.entity;

import com.tuisongbao.android.engine.common.TSBProgressCallback;
import com.tuisongbao.android.engine.log.LogUtil;

public class TSBChatOptions {
    private TSBProgressCallback mProgressCallback;
    private int mLastPercent = 0;

    public TSBChatOptions() {

    }

    public TSBChatOptions(TSBProgressCallback callback) {
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
