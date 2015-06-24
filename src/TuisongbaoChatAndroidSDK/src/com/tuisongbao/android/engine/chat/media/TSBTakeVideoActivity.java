package com.tuisongbao.android.engine.chat.media;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.tuisongbao.android.engine.R;
import com.tuisongbao.android.engine.log.LogUtil;

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
                LogUtil.debug(TAG, "To record video");
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
}
