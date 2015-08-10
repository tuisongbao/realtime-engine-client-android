package com.tuisongbao.engine.chat.media;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.common.EventEmitter;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.DownloadUtils;
import com.tuisongbao.engine.utils.StrUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class ChatVoiceRecorder extends EventEmitter {
    private static final String TAG = "TSB" + ChatVoiceRecorder.class.getSimpleName();
    private static final String EVENT_LOWER_THAN_MIN_DURATION = "lowerThanMinDuration";

    private MediaRecorder mRecorder;
    private String mCurrentVoiceFilePath;
    private int mMaxDuration = -1; // in millisecond
    private int mMinDuration = 2 * 1000; // in millisecond

    private long mStartTime;

    HashMap<String, ChatVoiceEventCallback> mEventCallbackHashMap = new HashMap<>();

    public ChatVoiceRecorder() {
        mRecorder = new MediaRecorder();
    }

    public interface ChatVoiceEventCallback {
        void onEvent();
    }

    public void start() {
        try {
            String filename = StrUtils.getTimestampStringOnlyContainNumber(new Date()) + ".3gp";
            File file = DownloadUtils.getOutputFile(filename, ChatMessage.TYPE.VOICE.getName());
            mCurrentVoiceFilePath = file.getAbsolutePath();

            mRecorder.reset();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mCurrentVoiceFilePath);
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

            // This must be after prepare(), because prepare() will block some time.
            mStartTime = new Date().getTime();
        } catch (IOException e) {
            LogUtil.error(TAG, e.getMessage());
        }
    }

    public String stop() {
        long endTime = new Date().getTime();
        try {
            mRecorder.stop();
        } catch (Exception e) {
            mRecorder.reset();
            LogUtil.error(TAG, e.getMessage());
        } finally {
            long duration = endTime - mStartTime;
            if (mMinDuration > 0 && duration < mMinDuration) {
                ChatVoiceEventCallback callback = mEventCallbackHashMap.get(EVENT_LOWER_THAN_MIN_DURATION);
                if (callback != null) {
                    callback.onEvent();
                }
                mCurrentVoiceFilePath = null;
            }
        }
        return mCurrentVoiceFilePath;
    }

    public void release() {
        try {
            mRecorder.reset();
            mRecorder.release();
        } catch (Exception e) {
            mRecorder.reset();
            LogUtil.error(TAG, e.getMessage());
        }
    }

    public void cancel() {
        try {
            mRecorder.stop();
            mRecorder.reset();
        } catch (Exception e) {
            mRecorder.reset();
            // FIXME: 15-8-8 stop failed -1007, if the recording time is short.
            LogUtil.error(TAG, e.getMessage());
        }
    }

    public void setMaxDuration(int duration, ChatVoiceEventCallback callback) {
        mMaxDuration = duration;
        mEventCallbackHashMap.put(MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED + "", callback);
    }

    public void setMinDuration(int duration, ChatVoiceEventCallback callback) {
        mMinDuration = duration;
        mEventCallbackHashMap.put(EVENT_LOWER_THAN_MIN_DURATION + "", callback);
    }
}
