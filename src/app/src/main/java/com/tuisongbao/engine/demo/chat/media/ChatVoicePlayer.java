package com.tuisongbao.engine.demo.chat.media;

import java.io.IOException;
import java.util.HashMap;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.tuisongbao.engine.chat.entity.TSBMessage;
import com.tuisongbao.engine.common.TSBEngineCallback;
import com.tuisongbao.engine.common.TSBProgressCallback;
import com.tuisongbao.engine.log.LogUtil;

public class ChatVoicePlayer implements OnPreparedListener, android.media.MediaPlayer.OnErrorListener {
    private static MediaPlayer mMediaPlayer;
    private static ChatVoicePlayer mInstance;
    private TSBMessage currentPlayingMessage;
    HashMap<TSBMessage, OnStopListener> stopListenerHashMap = new HashMap<TSBMessage, ChatVoicePlayer.OnStopListener>();
    HashMap<TSBMessage, OnErrorListener> errorListenerHashMap = new HashMap<TSBMessage, ChatVoicePlayer.OnErrorListener>();

    public interface OnStopListener {
        public void onStop();
    }

    public interface OnErrorListener {
        public void onError(String error);
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

    public void start(final TSBMessage message, final OnStopListener stopListener, final OnErrorListener errorListener
            , final TSBProgressCallback progressCallback) {
        try {
            stopLastMedia();
            startPlay(message, stopListener, errorListener, progressCallback);
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
        }
    }

    private void stopLastMedia() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            stop();
            callbackStopListener(currentPlayingMessage);
        }
    }

    private void startPlay(final TSBMessage message, final OnStopListener stopListener, final OnErrorListener errorListener
            , final TSBProgressCallback progressCallback) {
        currentPlayingMessage = message;

        if (errorListener != null) {
            errorListenerHashMap.put(message, errorListener);
        }

        message.downloadResource(new TSBEngineCallback<TSBMessage>() {

            @Override
            public void onSuccess(TSBMessage t) {
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(t.getResourcePath());
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
                    LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
                    callbackErrorListener(message, e.getMessage());
                }
            }

            @Override
            public void onError(int code, String errorMessage) {
                callbackErrorListener(message, errorMessage);
            }
        }, progressCallback);
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    private void callbackErrorListener(TSBMessage message, String errorMessage) {
        OnErrorListener errorListener = errorListenerHashMap.get(message);
        if (errorListener == null) {
            return;
        }
        errorListener.onError(errorMessage);
    }

    private void callbackStopListener(TSBMessage message) {
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
        LogUtil.info(LogUtil.LOG_TAG_CHAT, arg1 + "; " + arg2);
        return false;
    }
}
