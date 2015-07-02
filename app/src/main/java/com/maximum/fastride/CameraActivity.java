package com.maximum.fastride;

import java.io.IOException;
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
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

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
    private int midScreenWidth;
    private int midScreenHeight;
	
	private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {

		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			Log.d(LOG_TAG, "Number of faces detected: " + faces.length);
			mFaceView.setFaces(faces);

            for(int i = 0 ; i < faces.length ; i++){
                int posX = midScreenWidth - faces[0].rect.centerX();
                int posY = midScreenHeight + faces[0].rect.centerY();
                //myCustomView.setPoints(posX, posY);
            }
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

        Display display = getWindowManager().getDefaultDisplay();
        midScreenHeight = display.getHeight() / 2;
        midScreenWidth = display.getWidth() / 2;
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

        try {
            int nCamerasCount = Camera.getNumberOfCameras();
            mCamera = Camera.open();
        } catch(Exception ex) {
            Log.e(LOG_TAG, ex.getMessage());
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

	}

    private void initPreview(int width, int height) {
        if (mCamera!=null && mPreviewHolder.getSurface()!=null) {
            try {
                mCamera.setPreviewDisplay(mPreviewHolder);
            }
            catch (Throwable t) {
                Log.e(LOG_TAG, "Exception in setPreviewDisplay()", t);
            }

            Camera.Parameters parameters= mCamera.getParameters();
            Camera.Size size= getBestPreviewSize(width, height, parameters);
            if( size != null ) {
                parameters.setPreviewSize(size.width, size.height);
            }
        }
    }

    //
    // Implementation of SurfaceHolder.Callback
    //

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder,
                               int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d(LOG_TAG, "surfaceChanged");

        if( surfaceHolder.getSurface() == null ) {
            Log.d(LOG_TAG, "No surface yet");
			return;
		}

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes


        // start preview with new settings

        try {
            initPreview(width, height);

            mCamera.startPreview();

            if( isFaceDetectionEnabled() ) { // re-start face detection feature
                mCamera.setFaceDetectionListener(faceDetectionListener);
                mCamera.startFaceDetection();
            } else {
                Toast.makeText(this, getString(R.string.no_face_detection),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e){
            Log.d(LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }


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

	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        Log.d(LOG_TAG, "surfaceCreated");

        if( mCamera == null ) {
            Toast.makeText(this, "Camera was not opened yet",
                    Toast.LENGTH_LONG).show();
        }

        try {
            mCamera.setPreviewDisplay(mPreviewHolder);
            mCamera.startPreview();


            if( isFaceDetectionEnabled() ) { // start face detection feature
                mCamera.setFaceDetectionListener(faceDetectionListener);
                mCamera.startFaceDetection();
            } else {
                Toast.makeText(this, getString(R.string.no_face_detection),
                        Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(LOG_TAG, "surfaceDestroyed");

		mCamera.setPreviewCallback(null);
		mCamera.setFaceDetectionListener(null);
		mCamera.setErrorCallback(null);
        mCamera.stopFaceDetection();
		mCamera.release();
		mCamera = null;
		
	}

    // start face detection only *after* preview has started
    public boolean isFaceDetectionEnabled(){
        // Try starting Face Detection
        Camera.Parameters params = mCamera.getParameters();

        return (params.getMaxNumDetectedFaces() > 0) ? true : false;
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
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
