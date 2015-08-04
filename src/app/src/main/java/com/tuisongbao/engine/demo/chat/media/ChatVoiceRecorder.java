package com.tuisongbao.engine.demo.chat.media;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;

import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

public class ChatVoiceRecorder {
    public static final String INTENT_ACTION_TAKE_VIDEO = "com.tuisongbao.android.engine.media.TSBMediaRecorder.INTENT_ACTION_TAKE_VIDEO";
    private static final String TAG = "TSB" + "com.tuisongbao.android.engine.media.TSBMediaRecorder";

    private MediaRecorder mRecorder;
    private String mCurrentVoiceFileName;
    private int mMaxDuration = -1; // in millisecond

    HashMap<String, ChatVoiceEventCallback> mEventCallbackHashMap = new HashMap<String, ChatVoiceRecorder.ChatVoiceEventCallback>();

    public ChatVoiceRecorder() {
        mRecorder = new MediaRecorder();
    }

    public interface ChatVoiceEventCallback {
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
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            if (mMaxDuration > 0) {
                mRecorder.setMaxDuration(mMaxDuration);
            }
            mRecorder.setOnInfoListener(new OnInfoListener() {

                @Override
                public void onInfo(MediaRecorder recorder, int what, int extra) {
                    try {
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                            LogUtil.warn(TAG, "Maximum Duration Reached");
                            ChatVoiceEventCallback callback = mEventCallbackHashMap.get(what + "");
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

    public void setMaxDuration(int maxInMilliSeconds, ChatVoiceEventCallback callback) {
        mMaxDuration = maxInMilliSeconds;
        mEventCallbackHashMap.put(MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED + "", callback);
    }

}
