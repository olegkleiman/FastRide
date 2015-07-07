package com.maximum.fastride;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.maximum.fastride.cv.filters.Filter;
import com.maximum.fastride.cv.filters.NoneFilter;
import com.maximum.fastride.cv.filters.PortraCurveFilter;
import com.maximum.fastride.cv.filters.ProviaCurveFilter;
import com.maximum.fastride.cv.filters.RecolorRCFilter;
import com.maximum.fastride.cv.filters.VelviaCurveFilter;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class CameraCVActivity extends Activity implements CvCameraViewListener2 {

    private static final String LOG_TAG = "FR.CVCamera";

    private static final String STATE_CAMERA_INDEX = "cameraIndex";
    private static final String STATE_CURVE_FILTER_INDEX = "curveFilterIndex";
    private static final String STATE_MIXER_FILTER_INDEX = "mixerIndex";
    private static final String STATE_CONVULTION_FILTER_INDEX = "convultionIndex";

    private CameraBridgeViewBase mOpenCvCameraView;
    private int mCameraIndex;

    private Filter[] mCurveFilters;
    private int mCurveFilterIndex;
    private Filter[] mMixerFilters;
    private int mMixerFilterIndex;
    private Filter[] mConvolutionFilters;
    private int mConvultionFilterIndex;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(LOG_TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                    mCurveFilters = new Filter[] {
                        new NoneFilter(),
                        new PortraCurveFilter(),
                        new ProviaCurveFilter(),
                        new VelviaCurveFilter()
                    };

                    mMixerFilters = new Filter[] {
                            new NoneFilter(),
                            new RecolorRCFilter()
                    };

                    mConvolutionFilters = new Filter[] {
                            new NoneFilter()
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
            mCurveFilterIndex = savedInstanceState.getInt(STATE_CURVE_FILTER_INDEX, 0);
            mMixerFilterIndex = savedInstanceState.getInt(STATE_MIXER_FILTER_INDEX, 0);
            mConvultionFilterIndex = savedInstanceState.getInt(STATE_CONVULTION_FILTER_INDEX, 0);
        } else {
            mCameraIndex = 0;
            mCurveFilterIndex = 0;
            mMixerFilterIndex = 0;
            mConvultionFilterIndex = 0;
        }

        setContentView(R.layout.activity_camera_cv);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
        savedInstanceState.putInt(STATE_CURVE_FILTER_INDEX, mCurveFilterIndex);
        savedInstanceState.putInt(STATE_MIXER_FILTER_INDEX, mMixerFilterIndex);
        savedInstanceState.putInt(STATE_CONVULTION_FILTER_INDEX, mConvultionFilterIndex);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();

        if( mOpenCvCameraView != null )
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

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final Mat rgba = inputFrame.rgba();

        // Apply the active filters
        mCurveFilters[mCurveFilterIndex].apply(rgba, rgba);
        mCurveFilters[mCurveFilterIndex].apply(rgba, rgba);
        mConvolutionFilters[mConvultionFilterIndex].apply(rgba, rgba);

        return rgba;
    }
}
