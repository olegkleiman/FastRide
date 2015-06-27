package com.maximum.fastride;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

public class CameraActivity extends Activity 
		implements SurfaceHolder.Callback {

    private static final String LOG_TAG = "FR.Camera";

	private Camera mCamera;
	private SurfaceView mSurfaceView;
    private SurfaceHolder mPreviewHolder;
	private FaceOverlayView mFaceView;
	
	private int mDisplayRotation;
	private int mDisplayOrientation;
	
	private int mOrientation;
	private OrientationEventListener mOrientationEventListener;
	
	private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {

		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			Log.d(LOG_TAG, "Number of faces detected: " + faces.length);
			mFaceView.setFaces(faces);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        //mSurfaceView = new SurfaceView(this);
        //setContentView(mSurfaceView);
        setContentView(R.layout.activity_camera);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFormat(PixelFormat.UNKNOWN);

        mSurfaceView = (SurfaceView)findViewById(R.id.preview);

//		mFaceView = new FaceOverlayView(this);
//		addContentView(mFaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//
//		// Create and Start the Orientation Listener
//		mOrientationEventListener = new SimpleOrientationEventListener(this);
//		mOrientationEventListener.enable();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
        mPreviewHolder = mSurfaceView.getHolder();
        mPreviewHolder.addCallback(this);
	}
	
	@Override
	protected void onPause() {
		//mOrientationEventListener.disable();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		//mOrientationEventListener.enable();
		super.onResume();

        mCamera = Camera.open();
        startPreview();
	}

    private void initPreview(int width, int height) {
        if (mCamera!=null && mPreviewHolder.getSurface()!=null) {
            try {
                mCamera.setPreviewDisplay(mPreviewHolder);
            }
            catch (Throwable t) {
                Log.e(LOG_TAG, "Exception in setPreviewDisplay()", t);
            }
        }
    }

    private void startPreview() {
        mCamera.startPreview();
    }

        //
    // Implementation of SurfaceHolder.Callback
    //

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder,
                               int format, int width, int height) {
		
//		if( surfaceHolder.getSurface() == null ) {
//			return;
//		}
//
//		try {
//			mCamera.stopPreview();
//		}
//		catch(Exception e){
//			// Ignore...
//		}
//
//		// Get supported preview sizes:
//		Camera.Parameters parameters = mCamera.getParameters();
//		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//		Camera.Size previewSize = previewSizes.get(0);
//
//		// and set them:
//		parameters.setPreviewSize(previewSize.width, previewSize.height);
//		mCamera.setParameters(parameters);
//
//		// Now set the display orientation for the camera
//		mDisplayRotation = Util.getDisplayRotation(CameraActivity.this);
//		mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, 0);
//		mCamera.setDisplayOrientation(mDisplayOrientation);
//
//		if( mFaceView != null) {
//			mFaceView.setDisplayOrientation(mDisplayOrientation);
//		}
//
        initPreview(width, height);
		startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
//		mCamera = Camera.open();
//		mCamera.setFaceDetectionListener(faceDetectionListener);
//		mCamera.startFaceDetection();
//
//		try{
//			mCamera.setPreviewDisplay(surfaceHolder);
//		} catch(Exception e){
//			Log.e(LOG_TAG, "Could not preview the image", e);
//		}
	
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		
		mCamera.setPreviewCallback(null);
		mCamera.setFaceDetectionListener(null);
		mCamera.setErrorCallback(null);
		mCamera.release();
		mCamera = null;
		
	}
	
	private class SimpleOrientationEventListener extends OrientationEventListener{

		public SimpleOrientationEventListener(Context context) {
			super(context, SensorManager.SENSOR_DELAY_NORMAL);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			if( orientation == 0) 
				return;
			
			mOrientation = Util.roundOrientation(orientation, mOrientation);
		}
		
	}
}
