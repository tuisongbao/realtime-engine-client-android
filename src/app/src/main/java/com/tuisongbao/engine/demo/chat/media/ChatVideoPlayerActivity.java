package com.tuisongbao.engine.demo.chat.media;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.tuisongbao.engine.chat.message.ChatMessageVideoContent;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.download.DownloadManager;
import com.tuisongbao.engine.download.DownloadTask;

import java.io.File;

public class ChatVideoPlayerActivity extends Activity {
    VideoView videoView;
    MediaController mediaController;
    DownloadTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = (VideoView)findViewById(R.id.video);
        mediaController = new MediaController(this);
        final TextView textView = (TextView)findViewById(R.id.video_progress);

        ChatMessage message = ChatMessage.deserialize(DemoApplication.engine, getIntent().getStringExtra("message"));
        ChatMessageVideoContent content = (ChatMessageVideoContent)message.getContent();
        task = content.download(new EngineCallback<String>() {

            @Override
            public void onSuccess(final String filePath) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        File videoFile = new File(filePath);
                        if (videoFile.exists()) {
                            videoView.setVideoPath(filePath);
                            videoView.setMediaController(mediaController);
                            mediaController.setMediaPlayer(videoView);
                            videoView.requestFocus();
                        }
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {

            }
        }, new ProgressCallback() {

            @Override
            public void progress(final int percent) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(percent + "%");
                        if (percent == 100) {
                            textView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DownloadManager.getInstance().cancel(task);
    }
}
