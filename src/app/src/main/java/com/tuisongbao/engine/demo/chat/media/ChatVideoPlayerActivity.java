package com.tuisongbao.engine.demo.chat.media;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.tuisongbao.engine.demo.R;

public class ChatVideoPlayerActivity extends Activity {
    public static final String EXTRA_VIDEO_PATH="com.tuisongbao.android.engine.demo.ChatVideoPlayerActivity.video.path";

    VideoView videoView;
    MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String videoPathString = getIntent().getStringExtra(EXTRA_VIDEO_PATH);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = (VideoView)findViewById(R.id.video);
        mediaController = new MediaController(this);

        File videoFile = new File(videoPathString);
        if (videoFile.exists()) {
            videoView.setVideoPath(videoPathString);
            videoView.setMediaController(mediaController);
            mediaController.setMediaPlayer(videoView);
            videoView.requestFocus();
        }
    }
}
