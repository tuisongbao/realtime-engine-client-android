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

/**
 * <STRONG>语音消息帮助类</STRONG>
 *
 * <P>
 *     录制语音并存储到本地。
 */
public class ChatVoiceRecorder extends EventEmitter {
    private static final String TAG = "TSB" + ChatVoiceRecorder.class.getSimpleName();

    /**
     * 当出现错误时会触发该事件，处理方法接收一个回调参数，类型均为 {@code String}，表示错误原因
     */
    @SuppressWarnings("WeakerAccess")
    public static final String EVENT_ERROR = "chat_recorder:error";

    private static final String EVENT_SHORTER_THAN_MIN_DURATION = "chat_recorder:durationNotSatisfied";
    private static final String EVENT_MAX_DURATION_REACHED = "chat_recorder:maxDurationReached";

    private final MediaRecorder mRecorder;
    private String mCurrentVoiceFilePath;
    private int mMaxDuration = -1; // in millisecond
    private int mMinDuration = 2 * 1000; // in millisecond

    private long mStartTime;

    public ChatVoiceRecorder() {
        mRecorder = new MediaRecorder();
    }

    /**
     * 开始录音
     *
     * <P>
     *     初始化 {@code ChatVoiceRecorder}，过程中可能出现错误，需要通过绑定 {@link #EVENT_ERROR} 获取具体原因。
     */
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
                            trigger(EVENT_MAX_DURATION_REACHED, mMaxDuration);
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
            trigger(EVENT_ERROR, e.getMessage());
        }
    }

    /**
     * 停止录音
     *
     * <P>
     *     过程中可能出现错误，需要通过绑定 {@link #EVENT_ERROR} 获取具体原因。
     *
     * @return  录音文件的绝对路径
     */
    public String stop() {
        long endTime = new Date().getTime();
        try {
            mRecorder.stop();
        } catch (Exception e) {
            mRecorder.reset();
            trigger(EVENT_ERROR, e.getMessage());
        } finally {
            long duration = endTime - mStartTime;
            if (mMinDuration > 0 && duration < mMinDuration) {
                trigger(EVENT_SHORTER_THAN_MIN_DURATION, (int)duration, mMinDuration);
                mCurrentVoiceFilePath = null;
            }
        }
        return mCurrentVoiceFilePath;
    }

    /**
     * 释放录音资源
     *
     * <P>
     *     过程中可能出现错误，需要通过绑定 {@link #EVENT_ERROR} 获取具体原因。
     */
    public void release() {
        try {
            mRecorder.reset();
            mRecorder.release();

            unbind(EVENT_SHORTER_THAN_MIN_DURATION);
            unbind(EVENT_MAX_DURATION_REACHED);
            unbind(EVENT_ERROR);
        } catch (Exception e) {
            mRecorder.reset();
            trigger(EVENT_ERROR, e.getMessage());
        }
    }

    /**
     * 取消录音，同时临时文件将被删除
     *
     * <P>
     *     过程中可能出现错误，需要通过绑定 {@link #EVENT_ERROR} 获取具体原因。
     */
    public void cancel() {
        try {
            mRecorder.stop();
            mRecorder.reset();

            mCurrentVoiceFilePath = "";
        } catch (Exception e) {
            // After reset(), recorder become to `init` State, which will not cause IOException if call start() again.
            mRecorder.reset();
            trigger(EVENT_ERROR, e.getMessage());
            // FIXME: 15-8-8 stop failed -1007, if the recording time is short.
        }
    }

    /**
     * 设置最大时长以及事件处理方法，该方法接收一个参数，类型为 {@code int}
     *
     * <pre>
     *     new Emitter.Listener() {
     *        &#64;Override
     *        public void call(final Object... args) {
     *            Log.i(TAG, "录音超过最大时长 " + args[0] + " 毫秒");
     *        }
     *    }
     * </pre>
     *
     * @param duration  时长，单位为 <STRONG>毫秒</STRONG>
     * @param listener  处理方法
     */
    public void setMaxDuration(int duration, Listener listener) {
        mMaxDuration = duration;
        bind(EVENT_MAX_DURATION_REACHED, listener);
    }

    /**
     * 设置最小时长以及事件处理方法，该方法接收两个参数，类型均为 {@code int}
     *
     * <pre>
     *     new Emitter.Listener() {
     *        &#64;Override
     *        public void call(final Object... args) {
     *            Log.i(TAG, "录音时长为 " + args[0] + " 毫秒，小于最小时长 " + args[1] + " 毫秒");
     *        }
     *    }
     * </pre>
     *
     * @param duration 时长，单位为 <STRONG>毫秒</STRONG>
     * @param listener  处理方法
     */
    public void setMinDuration(int duration, Listener listener) {
        mMinDuration = duration;
        bind(EVENT_SHORTER_THAN_MIN_DURATION, listener);
    }
}
