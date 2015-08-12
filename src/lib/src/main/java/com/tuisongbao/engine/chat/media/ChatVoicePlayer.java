package com.tuisongbao.engine.chat.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;

import java.io.IOException;
import java.util.HashMap;

/**
 * <STRONG>语音消息帮助类</STRONG>
 *
 * <P>
 *     该类的使用为单例，应调用 {@code ChatVoicePlayer.getInstance()} 方法获取。并且该类<STRONG>只</STRONG>用于播放语音消息。
 */
public class ChatVoicePlayer implements OnPreparedListener, android.media.MediaPlayer.OnErrorListener {
    private static final String TAG = "TSB" + ChatVoicePlayer.class.getSimpleName();

    private static MediaPlayer mMediaPlayer;
    private static ChatVoicePlayer mInstance;
    private ChatMessage currentPlayingMessage;
    HashMap<ChatMessage, OnStopListener> stopListenerHashMap = new HashMap<>();
    HashMap<ChatMessage, OnErrorListener> errorListenerHashMap = new HashMap<>();

    public interface OnStopListener {
        void onStop();
    }

    public interface OnErrorListener {
        void onError(String error);
    }

    public ChatVoicePlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public static ChatVoicePlayer getInstance() {
        if (mInstance == null) {
            mInstance = new ChatVoicePlayer();
        }
        return mInstance;
    }

    /**
     * 开始播放
     *
     * <P>
     *     播放前，会自动停止上一个录音。
     *
     * @param message           语音消息实例
     * @param stopListener      停止事件的处理方法
     * @param errorListener     出错事件的处理方法
     * @param progressCallback  下载进度的处理方法
     */
    public void start(final ChatMessage message, final OnStopListener stopListener, final OnErrorListener errorListener
            , final ProgressCallback progressCallback) {
        try {
            stopLastMedia();
            startPlay(message, stopListener, errorListener, progressCallback);
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        mMediaPlayer.stop();
    }

    private void stopLastMedia() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            stop();
            callbackStopListener(currentPlayingMessage);
        }
    }

    private void startPlay(final ChatMessage message, final OnStopListener stopListener, final OnErrorListener errorListener
            , final ProgressCallback progressCallback) {
        currentPlayingMessage = message;

        if (errorListener != null) {
            errorListenerHashMap.put(message, errorListener);
        }

        message.downloadVoice(new EngineCallback<String>() {

            @Override
            public void onSuccess(String filePath) {
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(filePath);
                    mMediaPlayer.setOnPreparedListener(mInstance);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    if (stopListener != null) {
                        stopListenerHashMap.put(message, stopListener);
                        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer arg0) {
                                stop();
                                stopListener.onStop();
                            }
                        });
                    }
                } catch (IOException e) {
                    LogUtil.error(TAG, e);
                    callbackErrorListener(message, e.getMessage());
                }
            }

            @Override
            public void onError(ResponseError error) {
                callbackErrorListener(message, error.getMessage());
            }
        }, progressCallback);
    }

    private void callbackErrorListener(ChatMessage message, String errorMessage) {
        OnErrorListener errorListener = errorListenerHashMap.get(message);
        if (errorListener == null) {
            return;
        }
        errorListener.onError(errorMessage);
    }

    private void callbackStopListener(ChatMessage message) {
        OnStopListener stopListener = stopListenerHashMap.get(message);
        if (stopListener == null) {
            return;
        }
        stopListener.onStop();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer = mediaPlayer;
        mediaPlayer.start();
    }

    @Override
    public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
        LogUtil.info(TAG, arg1 + "; " + arg2);
        return false;
    }
}
