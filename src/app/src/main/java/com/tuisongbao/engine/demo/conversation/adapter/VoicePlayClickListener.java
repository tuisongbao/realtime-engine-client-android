package com.tuisongbao.engine.demo.conversation.adapter;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.tuisongbao.engine.chat.media.ChatVoicePlayer;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.R;

/**
 * Created by user on 15-9-2.
 */
public class VoicePlayClickListener implements View.OnClickListener {

    public boolean isPlaying;
    private AnimationDrawable animation;

    private BaseAdapter adapter;
    private Activity activity;
    private ChatMessage message;
    private ImageView voiceIconView;
    private ChatVoicePlayer player;
    private ProgressCallback progressCallback;

    public VoicePlayClickListener(Activity activity, ChatMessage message, final ImageView voiceIconView, BaseAdapter adapter, ProgressCallback progressCallback) {
        this.activity = activity;
        this.message = message;
        this.voiceIconView = voiceIconView;
        player = ChatVoicePlayer.getInstance();
        isPlaying = false;
        this.adapter = adapter;
        this.progressCallback = progressCallback;
    }

    private boolean isNotByMe(ChatMessage chatMessage){
        return !chatMessage.getFrom().equals(App.getInstance().getChatUser().getUserId());
    }

    private void showAnimation() {
        // play voice, and start animation
        if (isNotByMe(message)) {
            voiceIconView.setImageResource(R.anim.voice_from_icon);
        } else {
            voiceIconView.setImageResource(R.anim.voice_to_icon);
        }
        animation = (AnimationDrawable)voiceIconView.getDrawable();
    }

    public void stopAnimation() {
        animation.stop();
        if (isNotByMe(message)) {
            voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
        } else {
            voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
        }

        isPlaying = false;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (isPlaying) {
            return;
        }
        showAnimation();
        player.start(message, new ChatVoicePlayer.OnStopListener() {

            @Override
            public void onStop() {
                isPlaying = false;
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        stopAnimation();
                    }
                });
            }
        }, new ChatVoicePlayer.OnErrorListener() {

            @Override
            public void onError(String error) {
                isPlaying = false;
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        stopAnimation();
                    }
                });
            }
        }, progressCallback);
    }

}