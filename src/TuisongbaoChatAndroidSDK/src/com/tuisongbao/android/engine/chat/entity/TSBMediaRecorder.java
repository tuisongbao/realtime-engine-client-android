package com.tuisongbao.android.engine.chat.entity;

import java.io.IOException;
import java.util.Date;

import android.media.MediaRecorder;
import android.os.Environment;

import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBMediaRecorder {
    enum STATE { STOP, START, FINISHED, CANCELED };

    private MediaRecorder mRecorder;
    private String mCurrentVoiceFileName;
    private STATE mState = STATE.STOP;

    public TSBMediaRecorder() {
        mRecorder = new MediaRecorder();
    }

    public void start() {
        mRecorder.reset();

        mCurrentVoiceFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mCurrentVoiceFileName += StrUtil.getTimestampStringOnlyContainNumber(new Date()) + ".amr";

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(mCurrentVoiceFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
        }
        mRecorder.start();
    }

    public String finish() {
        mRecorder.stop();
        mRecorder.release();

        return mCurrentVoiceFileName;
    }

    public void cancel() {
        mRecorder.stop();
        mRecorder.release();
    }
}
