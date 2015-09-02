package com.tuisongbao.engine.demo.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.chat.message.ChatMessageContent;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;

import java.io.File;

/**
 * Created by user on 15-9-2.
 */
public class VoicePlayClickListener implements View.OnClickListener {

    ChatMessage message;
    ChatMessageContent voiceBody;
    ImageView voiceIconView;

    private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    ImageView iv_read_status;
    Activity activity;
    private ChatType chatType;
    private BaseAdapter adapter;

    public static boolean isPlaying = false;
    public static VoicePlayClickListener currentPlayListener = null;
    
    public VoicePlayClickListener(ChatMessage message, ImageView v,
                                  ImageView iv_read_status, BaseAdapter adapter, Activity activity,
                                  String username) {
        this.message = message;
        voiceBody = message.getContent();
        this.iv_read_status = iv_read_status;
        this.adapter = adapter;
        voiceIconView = v;
        this.activity = activity;
        this.chatType = message.getChatType();
    }

    private boolean isNotByMe(ChatMessage chatMessage){
        return !chatMessage.getFrom().equals(App.getInstance2().getChatUser().getUserId());
    }


    public void stopPlayVoice() {
        voiceAnimation.stop();
        if (!isNotByMe(message)) {
            voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
        } else {
            voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
        }
        // stop play voice
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
        ((ChatConversationActivity) activity).playMsgId = null;
        adapter.notifyDataSetChanged();
    }

    public void playVoice(String filePath) {
        if (!(new File(filePath).exists())) {
            return;
        }
        ((ChatConversationActivity) activity).playMsgId = message.getMessageId() + "";
        AudioManager audioManager = (AudioManager) activity
                .getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = new MediaPlayer();
        if (false) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        } else {
            audioManager.setSpeakerphoneOn(false);// 关闭扬声器
            // 把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            // TODO Auto-generated method stub
                            mediaPlayer.release();
                            mediaPlayer = null;
                            stopPlayVoice(); // stop animation
                        }

                    });
            isPlaying = true;
            currentPlayListener = this;
            mediaPlayer.start();
            showAnimation();

        } catch (Exception e) {
        }
    }

    // show the voice playing animation
    private void showAnimation() {
        // play voice, and start animation
        if (!isNotByMe(message)) {
            voiceIconView.setImageResource(R.anim.voice_from_icon);
        } else {
            voiceIconView.setImageResource(R.anim.voice_to_icon);
        }
        voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
        voiceAnimation.start();
    }

    @Override
    public void onClick(View v) {
        String st = activity.getResources().getString(
                R.string.Is_download_voice_click_later);
        if (isPlaying) {
            if (((ChatConversationActivity) activity).playMsgId != null
                    && ((ChatConversationActivity) activity).playMsgId.equals(message
                    .getMessageId())) {
                currentPlayListener.stopPlayVoice();
                return;
            }
            currentPlayListener.stopPlayVoice();
        }

        if (!isNotByMe(message)) {
            // for sent msg, we will try to play the voice file directly
            playVoice(voiceBody.getFile().getFilePath());
        } else {
            if(voiceBody.getFile() == null){
                return;
            }
            Log.i("voice path", voiceBody.getFile() + "");
            File file = new File(voiceBody.getFile().getFilePath());
            if (file.exists() && file.isFile())
                playVoice(voiceBody.getFile().getFilePath());
            else
                System.err.println("file not exist");
        }
    }
}