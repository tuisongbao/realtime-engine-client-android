package com.tuisongbao.android.engine.chat.media;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.media.MediaRecorder;
import android.os.Environment;

import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBMediaRecorder {
    public static final String INTENT_ACTION_TAKE_VIDEO = "com.tuisongbao.android.engine.media.TSBMediaRecorder.INTENT_ACTION_TAKE_VIDEO";

    private MediaRecorder mRecorder;
    private String mCurrentVoiceFileName;

    public TSBMediaRecorder() {

    }

    public void start() {
        try {
            mCurrentVoiceFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tuisongbao/voices/";
            mCurrentVoiceFileName += StrUtil.getTimestampStringOnlyContainNumber(new Date()) + ".3gp";
            File file = new File(mCurrentVoiceFileName);
            file.getParentFile().mkdirs();

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mCurrentVoiceFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
        }
    }

    public String stop() {
        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
        }

        return mCurrentVoiceFileName;
    }

    public void cancel() {
        mRecorder.stop();
        mRecorder.release();
    }
}
