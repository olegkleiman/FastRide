package com.maximum.fastride.fastcv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

/**
 * Created by Oleg Kleiman on 17-Aug-15.
 */
public class FastCVCameraView extends JavaCameraView {
        //implements Camera.PictureCallback {

    private static final String LOG_TAG = "FR.CVCameraView";

    public FastCVCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void stopPreview() {
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
    }

    public void startPreview() {
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
    }

    public void takePicture(Camera.PictureCallback pictureCallback) {

        try {

            Camera.Parameters cameraParams = mCamera.getParameters();
            mCamera.startPreview();

            mCamera.takePicture(null, null, pictureCallback);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }


    }

//    @Override
//    public void onPictureTaken(byte[] data, Camera camera) {
//
//        BitmapFactory.Options options=new BitmapFactory.Options();
//        Bitmap _bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//
////        mCamera.startPreview();
////        mCamera.setPreviewCallback(this);
//    }
}
