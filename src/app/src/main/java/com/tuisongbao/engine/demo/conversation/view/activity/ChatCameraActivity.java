package com.tuisongbao.engine.demo.conversation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.view.activity.*;
import com.tuisongbao.engine.demo.conversation.utils.DownloadUtils;
import com.tuisongbao.engine.demo.conversation.view.widght.ChatCameraPreview;
import com.tuisongbao.engine.utils.StrUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class ChatCameraActivity extends BaseActivity {
    public static final String ACTION_PHOTO = "com.tuisongbao.android.engine.TSBTakeVideoActivity.action.photo";
    public static final String ACTION_VIDEO = "com.tuisongbao.android.engine.TSBTakeVideoActivity.action.video";
    public static final String EXTRA_PHOTO = "com.tuisongbao.android.engine.TSBTakeVideoActivity.result.photo";
    public static final String EXTRA_VIDEO = "com.tuisongbao.android.engine.TSBTakeVideoActivity.result.video";

    private static final String TAG = "TSB" + "com.tuisongbao.android.engine.chat.media.TSBTakeVideoActivity";

    private ChatCameraPreview mPreview;
    private Activity mActivity;
    private Camera mCamera;
    private MediaRecorder mRecorder;
    private boolean isRecording = false;
    private String mResourcePath = "";
    private Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mActivity = this;

        mPreview = new ChatCameraPreview(this, (SurfaceView)findViewById(R.id.surfaceView));
        mPreview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ((RelativeLayout) findViewById(R.id.layout_camera_preview)).addView(mPreview);
        mPreview.setKeepScreenOn(true);

        actionButton = (Button) findViewById(R.id.button_camera);
        String action = getIntent().getAction();
        if (StrUtils.isEqual(action, ACTION_PHOTO)) {
            actionButton.setText("Click to take picture");
        } else if (StrUtils.isEqual(action, ACTION_VIDEO)) {
            actionButton.setText("Click to start");
        }
        actionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String action = getIntent().getAction();
                if (StrUtils.isEqual(action, ACTION_PHOTO)) {
                    takePhoto();
                } else if (StrUtils.isEqual(action, ACTION_VIDEO)) {
                    takeVideo();
                }
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
                mPreview.setCamera(mCamera);
                // Adjust the orientation to let the view display the right frame.
                mPreview.setCameraDisplayOrientation(this, 0);
                mCamera.startPreview();
            } catch (RuntimeException ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
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
                mResourcePath = StrUtils.getTimestampStringOnlyContainNumber(new Date()) + ".jpg";
                final File outFile = DownloadUtils.getOutputFile(mResourcePath, "images");

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                refreshGallery(outFile);

                Intent intent = new Intent();
                mResourcePath = outFile.getAbsolutePath();
                intent.putExtra(EXTRA_PHOTO, mResourcePath);
                setResult(RESULT_OK, intent);

                finish();
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

    private void takePhoto() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Toast.makeText(mActivity, "Photo has been token, begin to save...", Toast.LENGTH_SHORT).show();
                new SaveImageTask().execute(data);
                resetCam();
            }
        });
    }

    private void takeVideo() {
        if (isRecording) {
            mRecorder.stop();

            Intent intent = new Intent();
            intent.putExtra(EXTRA_VIDEO, mResourcePath);
            setResult(RESULT_OK, intent);

            finish();
            return;
        }

        isRecording = !isRecording;
        actionButton.setText("Click to stop");

        try {
            mRecorder = new MediaRecorder();
            // Allow MediaRecorder access to the camera hardware
            mCamera.unlock();
            mRecorder.setCamera(mCamera);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // 352 x 288 in pixels
            mRecorder.setProfile(CamcorderProfile
                    .get(CamcorderProfile.QUALITY_CIF));

            mResourcePath = DownloadUtils.getOutputFile(StrUtils.getTimestampStringOnlyContainNumber(new Date()) + ".mp4", "videos").getAbsolutePath();
            mRecorder.setOutputFile(mResourcePath);

            mRecorder.setPreviewDisplay(mPreview.getSurface());

            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
