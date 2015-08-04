package com.tuisongbao.engine.demo.chat.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.callback.TSBProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;

import java.io.IOException;
import java.util.HashMap;

public class ChatVoicePlayer implements OnPreparedListener, android.media.MediaPlayer.OnErrorListener {
    private static final String TAG = "TSB" + ChatVoicePlayer.class.getSimpleName();

    private static MediaPlayer mMediaPlayer;
    private static ChatVoicePlayer mInstance;
    private ChatMessage currentPlayingMessage;
    HashMap<ChatMessage, OnStopListener> stopListenerHashMap = new HashMap<ChatMessage, ChatVoicePlayer.OnStopListener>();
    HashMap<ChatMessage, OnErrorListener> errorListenerHashMap = new HashMap<ChatMessage, ChatVoicePlayer.OnErrorListener>();

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

    public void start(final ChatMessage message, final OnStopListener stopListener, final OnErrorListener errorListener
            , final TSBProgressCallback progressCallback) {
        try {
            stopLastMedia();
            startPlay(message, stopListener, errorListener, progressCallback);
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    private void stopLastMedia() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            stop();
            callbackStopListener(currentPlayingMessage);
        }
    }

    private void startPlay(final ChatMessage message, final OnStopListener stopListener, final OnErrorListener errorListener
            , final TSBProgressCallback progressCallback) {
        currentPlayingMessage = message;

        if (errorListener != null) {
            errorListenerHashMap.put(message, errorListener);
        }

        message.downloadResource(new TSBEngineCallback<ChatMessage>() {

            @Override
            public void onSuccess(ChatMessage t) {
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

    public void stop() {
        mMediaPlayer.stop();
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
