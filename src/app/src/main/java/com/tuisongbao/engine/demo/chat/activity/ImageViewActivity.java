package com.tuisongbao.engine.demo.chat.activity;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.callback.TSBProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;

/**
 * Created by root on 15-8-6.
 */
public class ImageViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        final TextView textView = (TextView)findViewById(R.id.text_view_progress);

        String messageString = getIntent().getStringExtra("message");
        ChatMessage message = ChatMessage.deserialize(DemoApplication.engine, messageString);
        message.downloadImage(true, new TSBEngineCallback<String>() {
            @Override
            public void onSuccess(final String path) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView view = (ImageView) findViewById(R.id.image_view);
                        view.setImageBitmap(BitmapFactory.decodeFile(path));
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {

            }
        }, new TSBProgressCallback() {
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
}
