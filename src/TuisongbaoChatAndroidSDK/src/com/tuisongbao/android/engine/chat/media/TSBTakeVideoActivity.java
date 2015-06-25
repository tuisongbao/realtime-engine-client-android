package com.tuisongbao.android.engine.chat.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tuisongbao.android.engine.R;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.DownloadUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBTakeVideoActivity extends Activity {
    private static final String TAG = "com.tuisongbao.android.engine.chat.media.TSBTakeVideoActivity";

    private TSBCameraPreview mPreview;
    private Context mContext;
    private Activity mActivity;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mContext = this;
        mActivity = this;

        mPreview = new TSBCameraPreview(this, (SurfaceView)findViewById(R.id.surfaceView));
        mPreview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ((RelativeLayout) findViewById(R.id.layout_camera_preview)).addView(mPreview);
        mPreview.setKeepScreenOn(true);

        Button testButton = (Button) findViewById(R.id.button_camera);
        testButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mCamera.takePicture(null, null, new PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Log.d(TAG, "onPictureTaken");
                        Toast.makeText(mActivity, "Picture tokend, begin to save...", Toast.LENGTH_SHORT).show();
                        new SaveImageTask().execute(data);
                        resetCam();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                mCamera = Camera.open(0);
                mCamera.startPreview();
                mPreview.setCamera(mCamera);
                mPreview.setCameraDisplayOrientation(this, 0);
            } catch (RuntimeException ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        if(mCamera != null) {
            mCamera.stopPreview();
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
        super.onPause();
    }

    private void resetCam() {
        mCamera.startPreview();
        mPreview.setCamera(mCamera);
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;
            try {
                String fileNameString = StrUtil.getTimestampStringOnlyContainNumber(new Date()) + ".jpg";
                final File outFile = DownloadUtil.getOutputFile(fileNameString, "images");

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                LogUtil.info(TAG, "SaveImageTask - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                mActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "Save picture into" + outFile.getAbsolutePath() + " succefully", Toast.LENGTH_SHORT).show();
                    }
                });
                refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }
}
