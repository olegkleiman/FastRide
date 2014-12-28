package com.maximum.fastride;

import java.util.List;

import android.app.Activity;
import android.content.Context;
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

	private Camera mCamera;
	private SurfaceView mView;
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

	private static final String LOG_TAG = "fast_ride";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_camera);
		
		mView = new SurfaceView(this);
		setContentView(mView);
		
		mFaceView = new FaceOverlayView(this);
		addContentView(mFaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		// Create and Start the Orientation Listener
		mOrientationEventListener = new SimpleOrientationEventListener(this);
		mOrientationEventListener.enable();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		SurfaceHolder holder = mView.getHolder();
		holder.addCallback(this);
	}
	
	@Override
	protected void onPause() {
		mOrientationEventListener.disable();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		mOrientationEventListener.enable();
		super.onResume();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int arg1, int arg2, int arg3) {
		
		if( surfaceHolder.getSurface() == null ) {
			return;
		}
		
		try {
			mCamera.stopPreview();
		}
		catch(Exception e){
			// Ignore...
		}
		
		// Get supported preview sizes:
		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
		Camera.Size previewSize = previewSizes.get(0);
		
		// and set them:
		parameters.setPreviewSize(previewSize.width, previewSize.height);
		mCamera.setParameters(parameters);
		
		// Now set the display orientation for the camera
		mDisplayRotation = Util.getDisplayRotation(CameraActivity.this);
		mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, 0);
		mCamera.setDisplayOrientation(mDisplayOrientation);
		
		if( mFaceView != null) {
			mFaceView.setDisplayOrientation(mDisplayOrientation);
		}
		
		mCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		mCamera = Camera.open();
		mCamera.setFaceDetectionListener(faceDetectionListener);
		mCamera.startFaceDetection();
		
		try{
			mCamera.setPreviewDisplay(surfaceHolder);
		} catch(Exception e){
			Log.e(LOG_TAG, "Could not preview the image", e);
		}
	
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
