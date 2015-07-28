package com.maximum.fastride;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.maximum.fastride.cv.filters.Filter;
import com.maximum.fastride.cv.filters.ImageDetectionFilter;
import com.maximum.fastride.cv.filters.NoneFilter;
import com.maximum.fastride.cv.filters.PortraCurveFilter;
import com.maximum.fastride.cv.filters.ProviaCurveFilter;
import com.maximum.fastride.cv.filters.RecolorCMVFilter;
import com.maximum.fastride.cv.filters.RecolorRCFilter;
import com.maximum.fastride.cv.filters.RecolorRGVFilter;
import com.maximum.fastride.cv.filters.StrokeEdgesFilter;
import com.maximum.fastride.cv.filters.VelviaCurveFilter;
import com.maximum.fastride.fastcv.FastCVWrapper;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.io.IOException;
import java.util.ArrayList;

public class CameraCVActivity extends Activity implements CvCameraViewListener2 {

    private static final String LOG_TAG = "FR.CVCamera";

    private static final String STATE_CAMERA_INDEX = "cameraIndex";
    private static final String STATE_CURVE_FILTER_INDEX = "curveFilterIndex";
    private static final String STATE_MIXER_FILTER_INDEX = "mixerIndex";
    private static final String STATE_CONVOLUTION_FILTER_INDEX = "convolutionIndex";
    private static final String STATE_DETECTION_FILTER_INDEX = "detectionIndex";

    private CameraBridgeViewBase mOpenCvCameraView;
    private int mCameraIndex;

    private boolean mIsCameraFrontFacing;
    private int mNumCameras;

    private Filter[] mCurveFilters;
    private int mCurveFilterIndex;
    private Filter[] mMixerFilters;
    private int mMixerFilterIndex;
    private Filter[] mConvolutionFilters;
    private int mConvolutionFilterIndex;
    private Filter[] mImageDetectionFilters;
    private int mImageDetectionFilterIndex;

    private String[] mCurveFilterNames = { "None", "Portra", "Provia", "Velvia" };

    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(LOG_TAG, "OpenCV loaded successfully");

                    System.loadLibrary("fastcvUtils");

                    if( mOpenCvCameraView != null)
                        mOpenCvCameraView.enableView();

                    mCurveFilters = new Filter[] {
                        new NoneFilter(),
                        new PortraCurveFilter(),
                        new ProviaCurveFilter(),
                        new VelviaCurveFilter()
                    };

                    mMixerFilters = new Filter[] {
                        new NoneFilter(),
                        new RecolorRCFilter(),
                        new RecolorRGVFilter(),
                        new RecolorCMVFilter()
                    };

                    mConvolutionFilters = new Filter[] {
                        new NoneFilter(),
                        new StrokeEdgesFilter()
                    };

                    final Filter starryNightFilter, akbarHuntingFilter;
                    try{
                        starryNightFilter = new ImageDetectionFilter(CameraCVActivity.this,
                                                               R.drawable.starry_night);
                        akbarHuntingFilter = new ImageDetectionFilter(CameraCVActivity.this,
                                                                R.drawable.akbar_hunting_with_cheetahs);

                    } catch (IOException ex) {
                        Log.e(LOG_TAG, ex.getMessage());
                        break;
                    }

                    mImageDetectionFilters = new Filter[] {
                        new NoneFilter(),
                        starryNightFilter,
                        akbarHuntingFilter
                    };


                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraCVActivity() {

        Log.i(LOG_TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if( savedInstanceState != null ) {
            mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
            mImageDetectionFilterIndex = savedInstanceState.getInt(STATE_DETECTION_FILTER_INDEX, 0);
            mCurveFilterIndex = savedInstanceState.getInt(STATE_CURVE_FILTER_INDEX, 0);
            mMixerFilterIndex = savedInstanceState.getInt(STATE_MIXER_FILTER_INDEX, 0);
            mConvolutionFilterIndex = savedInstanceState.getInt(STATE_CONVOLUTION_FILTER_INDEX, 0);
        } else {
            mCameraIndex = 0;
            mImageDetectionFilterIndex = 0;
            mCurveFilterIndex = 0;
            mMixerFilterIndex = 0;
            mConvolutionFilterIndex = 0;
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraIndex, cameraInfo);
        mIsCameraFrontFacing = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
        mNumCameras = Camera.getNumberOfCameras();

        setContentView(R.layout.activity_camera_cv);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
        //mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_surface_view);
        if( mOpenCvCameraView != null ) {
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);

            mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
        savedInstanceState.putInt(STATE_DETECTION_FILTER_INDEX, mImageDetectionFilterIndex);
        savedInstanceState.putInt(STATE_CURVE_FILTER_INDEX, mCurveFilterIndex);
        savedInstanceState.putInt(STATE_MIXER_FILTER_INDEX, mMixerFilterIndex);
        savedInstanceState.putInt(STATE_CONVOLUTION_FILTER_INDEX, mConvolutionFilterIndex);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera_cv, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch( item.getItemId() ) {

            case R.id.menu_next_detection_filter:
                mImageDetectionFilterIndex++;
                if( mImageDetectionFilterIndex == mImageDetectionFilters.length ) {
                    mImageDetectionFilterIndex = 0;
                }
                return true;

            case R.id.menu_next_curve_filter:
                this.setTitle(mCurveFilterNames[mCurveFilterIndex]);
                mCurveFilterIndex++;
                if( mCurveFilterIndex == mCurveFilters.length ) {
                    mCurveFilterIndex = 0;
                }
                return true;

            case R.id.menu_next_mix_filter:
                mMixerFilterIndex++;
                if( mMixerFilterIndex == mMixerFilters.length ) {
                    mMixerFilterIndex = 0;
                }
                return  true;

            case R.id.menu_next_conv_filter:
                mConvolutionFilterIndex++;
                if( mMixerFilterIndex == mMixerFilters.length) {
                    mConvolutionFilterIndex = 0;
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if( !OpenCVLoader.initDebug() ) {
            Log.d(LOG_TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");

            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11,
                    this,
                    mLoaderCallback);
        } else {
            Log.d(LOG_TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if( mOpenCvCameraView != null )
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

    //        final Mat rgba = inputFrame.rgba();

////        if( mImageDetectionFilters != null ) {
////            mImageDetectionFilters[mImageDetectionFilterIndex].apply(rgba, rgba);
////        }
//
//        // Apply the active filters
//        mCurveFilters[mCurveFilterIndex].apply(rgba, rgba);
//        mMixerFilters[mMixerFilterIndex].apply(rgba, rgba);
//        mConvolutionFilters[mConvolutionFilterIndex].apply(rgba, rgba);
//
//        if( mIsCameraFrontFacing ) {
//            // Mirror (horizontally flip) the preview
//            Core.flip(rgba, rgba, 1);
//        }

        // input frame has RGBA format
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        //FastCVWrapper cvWrapper = new FastCVWrapper();
        FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());

//        Mat mat = new Mat();
//        Scalar fontColor = new Scalar(0,0,0);
//        Point fontPoint = new Point();
//        Core.putText(mat, "Test",
//                fontPoint, Core.FONT_HERSHEY_PLAIN, 1.5, fontColor,
//                2, Core.LINE_AA, false);

        return mRgba;
    }

    public native void FindFeatures(long matAddrGr, long matAddrRgba);

}
