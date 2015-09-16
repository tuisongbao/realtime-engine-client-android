package com.tuisongbao.engine.demo.chat;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.apkfuns.logutils.LogUtils;
import com.tuisongbao.engine.chat.message.ChatMessage.TYPE;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.task.LoadLocalBigImgTask;
import com.tuisongbao.engine.demo.chat.utils.DownloadUtils;
import com.tuisongbao.engine.demo.chat.utils.ImageCache;
import com.tuisongbao.engine.demo.widght.TouchImageView.TouchImageView;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.io.File;

/**
 * Created by user on 15-9-16.
 */
@EActivity(R.layout.activity_show_big_image)
public class ShowBigImageActivity extends BaseActivity {
    public static final String REMOTEPATH = "remotePath";
    public static final String LOCALFULLSIZEPATH = "LOCALFULLSIZEPATH";

    @ViewById(R.id.pb_load_local)
    ProgressBar loadLocalPb;

    @ViewById(R.id.image)
    TouchImageView image;

    private int default_res = R.drawable.default_image;
    private ProgressDialog pd;
    private Bitmap bitmap;
    private boolean isDownloaded;

    @Extra(ShowBigImageActivity.REMOTEPATH)
    String remotePath;

    @Extra(ShowBigImageActivity.LOCALFULLSIZEPATH)
    String localFullSizePath;

    @AfterViews
    void afterViews(){
        default_res = getIntent().getIntExtra("default_image", R.drawable.default_image);
        if (localFullSizePath != null && new File(localFullSizePath).exists()) {
            LogUtils.i("showbigimage file exists. directly show it");
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
             int screenWidth = metrics.widthPixels;
             int screenHeight =metrics.heightPixels;
            bitmap = ImageCache.getInstance().get(localFullSizePath);
            if (bitmap == null) {
                LoadLocalBigImgTask task = new LoadLocalBigImgTask(this,
                        localFullSizePath, image, loadLocalPb,
                        screenWidth,
                        screenHeight);
                if (android.os.Build.VERSION.SDK_INT > 10) {
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    task.execute();
                }
            }else {
                image.setImageBitmap(bitmap);
            }
        }else if (remotePath != null) { // 去服务器下载图片
            System.err.println("download remote image");
            downloadImage(remotePath);
        } else {
            image.setImageResource(default_res);
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @AfterExtras
    void afterExtras(){
        LogUtils.i("show big image localFullSizePath: %s remotePath: %s", localFullSizePath, remotePath);
    }

    /**
     * 下载图片
     *
     * @param remoteFilePath
     */
    private void downloadImage(final String remoteFilePath) {
        String str1 = getResources().getString(R.string.Download_the_pictures);
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(str1);
        pd.show();

        DownloadUtils.downloadResourceIntoLocal(remoteFilePath, TYPE.IMAGE, new EngineCallback<String>() {
            @Override
            public void onSuccess(final String s) {
                LogUtils.i(s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(
                                metrics);
                        Bitmap bmp = BitmapFactory.decodeFile(s);
                        if (bmp == null) {
                            image.setImageResource(default_res);
                        } else {
                            image.setImageBitmap(bmp);
                            isDownloaded = true;
                        }

                        if (pd != null) {
                            pd.dismiss();
                        }
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {
                Log.e("###", "offline file transfer error:" + error);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        image.setImageResource(default_res);
                    }
                });
            }
        }, new ProgressCallback(){

            @Override
            public void progress(final int progress) {
                final String str2 = getResources().getString(
                        R.string.Download_the_pictures_new);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.setMessage(str2 + progress + "%");
                    }
                });

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isDownloaded)
            setResult(RESULT_OK);
        finish();
    }
}
