package com.tuisongbao.android.engine.chat.media;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;

import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBMediaRecorder {
    public static final String INTENT_ACTION_TAKE_VIDEO = "com.tuisongbao.android.engine.media.TSBMediaRecorder.INTENT_ACTION_TAKE_VIDEO";
    private static final String TAG = "com.tuisongbao.android.engine.media.TSBMediaRecorder";

    private MediaRecorder mRecorder;
    private String mCurrentVoiceFileName;
    private int mMaxDuration = -1; // in millisecond

    HashMap<String, TSBMediaEventCallback> mEventCallbackHashMap = new HashMap<String, TSBMediaRecorder.TSBMediaEventCallback>();

    public TSBMediaRecorder() {
        mRecorder = new MediaRecorder();
    }

    public interface TSBMediaEventCallback {
        public void onEvent();
    }

    public void start() {
        try {
            mCurrentVoiceFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tuisongbao/voices/";
            mCurrentVoiceFileName += StrUtil.getTimestampStringOnlyContainNumber(new Date()) + ".3gp";
            File file = new File(mCurrentVoiceFileName);
            file.getParentFile().mkdirs();

            mRecorder.reset();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mCurrentVoiceFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            if (mMaxDuration > 0) {
                mRecorder.setMaxDuration(mMaxDuration);
            }
            mRecorder.setOnInfoListener(new OnInfoListener() {

                @Override
                public void onInfo(MediaRecorder recorder, int what, int extra) {
                    try {
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                            LogUtil.warn(TAG, "Maximum Duration Reached");
                            TSBMediaEventCallback callback = mEventCallbackHashMap.get(what + "");
                            if (callback != null) {
                                callback.onEvent();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            LogUtil.error(TAG, e);
        }
    }

    public String stop() {
        try {
            mRecorder.stop();
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }

        return mCurrentVoiceFileName;
    }

    public void release() {
        try {
            mRecorder.release();
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    public void cancel() {
        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    public void setMaxDuration(int maxInMilliSeconds, TSBMediaEventCallback callback) {
        mMaxDuration = maxInMilliSeconds;
        mEventCallbackHashMap.put(MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED + "", callback);
    }

}
