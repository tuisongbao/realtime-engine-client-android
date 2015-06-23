package com.tuisongbao.android.engine.chat.media;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.tuisongbao.android.engine.R;
import com.tuisongbao.android.engine.log.LogUtil;

public class TSBTakeVideoActivity extends Activity {
    private static final String TAG = "com.tuisongbao.android.engine.chat.media.TSBTakeVideoActivity";

    public static class TestView extends SurfaceView implements SurfaceHolder.Callback {
        Activity mActivity;
        SurfaceHolder mHolder;
        Size mPreviewSize;
        Camera mCamera;
        List<Size> mSupportedPreviewSizes = null;

        public TestView(Context context) {
            super(context);
        }

        public TestView(Context context, AttributeSet attrs) {
            super(context, attrs);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                 mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        }

        public TestView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public void setActivity(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
                int arg3) {
            LogUtil.info(TAG, "Surface changed");
            Camera.Parameters parameters = mCamera.getParameters();
            requestLayout();

            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            for (Size size : previewSizes) {
                Log.d(TAG, size.height + ":" + size.width);
            }

            // TODO: select the most suitable size
            Camera.Size previewSize = previewSizes.get(11);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            mCamera.setParameters(parameters);
            setCameraDisplayOrientation(mActivity, 1, mCamera);

            mCamera.startPreview();
        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
            // TODO Auto-generated method stub
            LogUtil.info(TAG, "Surface created");
            mCamera = Camera.open(1);
            try {
                mCamera.setPreviewDisplay(getHolder());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
            if (mCamera != null) {
                mCamera.stopPreview();
                LogUtil.info(TAG, "Surface destroyed");
            }
        }

        public void setCameraDisplayOrientation(Activity activity,
                int cameraId, android.hardware.Camera camera) {
            android.hardware.Camera.CameraInfo info =
                    new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            int rotation = activity.getWindowManager().getDefaultDisplay()
                    .getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            camera.setDisplayOrientation(result);
        }

        public void setCamera(Camera camera) {
            if (mCamera == camera) { return; }

            stopPreviewAndFreeCamera();

            Log.e(TAG, "setCamera");
            mCamera = camera;

            if (mCamera != null) {
                List<Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
                mSupportedPreviewSizes = localSizes;
                requestLayout();

                try {
                    Log.e(TAG, "mHolder" + mHolder);
                    mCamera.setPreviewDisplay(mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Important: Call startPreview() to start updating the preview
                // surface. Preview must be started before you can take a picture.
                mCamera.startPreview();
                Log.e(TAG, "startPreview");
            }
        }

        /**
         * When this function returns, mCamera will be null.
         */
        private void stopPreviewAndFreeCamera() {

            if (mCamera != null) {
                // Call stopPreview() to stop updating the preview surface.
                mCamera.stopPreview();

                // Important: Call release() to release the camera for use by other
                // applications. Applications should release the camera immediately
                // during onPause() and re-open() it during onResume()).
                mCamera.release();

                mCamera = null;
            }
        }
    }

    private TestView mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mPreview = (TestView) findViewById(R.id.preview_camera);
        mPreview.setActivity(this);

        Button testButton = (Button) findViewById(R.id.button_camera);
        testButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LogUtil.debug(TAG, "To record video");
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        releaseCameraAndPreview();
        super.onBackPressed();
    }

    private void releaseCameraAndPreview() {
        mPreview.setCamera(null);
    }
}
