package com.tuisongbao.engine.demo.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.chat.message.ChatMessageVideoContent;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.Utils;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.io.File;

/**
 * Created by user on 15-9-6.
 */
@EActivity(R.layout.activity_show_video)
public class ChatVideoPlayerActivity extends BaseActivity {

    public static final String LOCALPATH = "localpath";
    public static final String REMOTEPATH = "remotepath";
    public static final String CHATMESSAGE = "chatmessage";
    @ViewById(R.id.loading_layout)
    RelativeLayout loadingLayout;

    @ViewById(R.id.progressBar)
    ProgressBar progressBar;

    @Extra(LOCALPATH)
    String localFilePath;

    @Extra(REMOTEPATH)
    String remotepath;

    @Extra(CHATMESSAGE)
    String chatMessageStr;

    ChatMessage chatMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @AfterExtras
    public void afterExtras() {
        if (localFilePath != null && new File(localFilePath).exists()) {
            showLocalVideo(localFilePath);
        } else if (!TextUtils.isEmpty(remotepath) && !remotepath.equals("null")) {
            try{
                chatMessage = ChatMessage.deserialize(App.getInstance2().getEngine(), chatMessageStr);
                ChatMessageVideoContent content = (ChatMessageVideoContent)chatMessage.getContent();
                content.download(new EngineCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        loadingLayout.setVisibility(View.GONE);
                        progressBar.setProgress(0);
                        showLocalVideo(localFilePath);
                    }

                    @Override
                    public void onError(ResponseError error) {
                        Log.e("###", "offline file transfer error:"  + error);
                        File file = new File(localFilePath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }, new ProgressCallback() {
                    @Override
                    public void progress(int percent) {
                        progressBar.setProgress(percent);
                    }
                });
            }catch (Exception e){
                Utils.showShortToast(this, "播放失败");
                finish();
            }
        } else {

        }
    }

    /**
     * 播放本地视频
     *
     * @param localPath
     *            视频路径
     */
    private void showLocalVideo(String localPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(localPath)), "video/mp4");
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
