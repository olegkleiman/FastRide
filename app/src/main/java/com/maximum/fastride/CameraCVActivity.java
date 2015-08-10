package com.maximum.fastride;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
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
import com.maximum.fastride.fastcv.DetectionBasedTracker;
import com.maximum.fastride.fastcv.FastCVWrapper;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

//import org.opencv.features2d.KeyPoint;

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

    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;

    Scalar mCameraFontColor = new Scalar(255, 0, 0, 255);
    String mCameraDirective;
    File mDetectorConfigFile;

    File mCascadeFile;
    CascadeClassifier mJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    // FD
    //
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private int         mDetectorType = JAVA_DETECTOR;
    private String[]    mDetectorName;
    private float       mRelativeFaceSize = 0.2f;
    private int         mAbsoluteFaceSize = 0;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(LOG_TAG, "OpenCV loaded successfully");

                    System.loadLibrary("fastcvUtils");

                    try {

                        // Since we're going to load cascade classifier
                        // from within native C++ code, the cascade file
                        // must be copied to the directory accesible from native code.
                        // Thus, we're reading the desired file from the resources
                        // and save it to app data directory.
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");

                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while( (bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }

                        os.close();
                        is.close();

                        mJavaDetector =
                                new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(LOG_TAG, "Failed to load cascade filter");
                            mJavaDetector = null;
                        } else
                            Log.i(LOG_TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch(IOException ex) {
                        Log.e(LOG_TAG, "Failed to load cascade. Excception: "  + ex.getMessage());

                    }

                    if( mOpenCvCameraView != null)
                        mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraCVActivity() {

        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(LOG_TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if( savedInstanceState != null ) {
            mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
        } else {
            mCameraIndex = 0;
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

        mCameraDirective = getString(R.string.camera_directive_1);

        File outputDir = getCacheDir();
        try {
            mDetectorConfigFile = File.createTempFile("orbDetectorParams", ".YAML", outputDir);
            String fastConfig = "%YAML:1.0\nthreshold: 10\nnonmaxSuppression: 1\ntype : 1\n";
            String orbConfig  = "%YAML:1.0\nscaleFactor: 1.2\nnLevels: 8\nfirstLevel: 0 \nedgeThreshold: 31\npatchSize: 31\nWTA_K: 2\nscoreType: 1\nnFeatures: 500\n";
            writeToFile(mDetectorConfigFile, fastConfig);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);

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

        if( item.getItemId() == R.id.menu_cv_params ) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }

        return super.onOptionsItemSelected(item);

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
        // Roughly, it's an analog of System.loadLibrary('opencv_java3') - meaning .so library
        // In our case it is supposed to always return false, because we aare statically linked with opencv_java3.so
        // (in jniLbs/<platform> folder.
        //
        // Such way of linking allowed for running without OpenCV Manager (https://play.google.com/store/apps/details?id=org.opencv.engine&hl=en)
            Log.d(LOG_TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");

            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0,
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

    long mFramesReceived = 0;
    long mExecutionTime = 0;

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        // input frame has RGBA format
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

//        mGray.height();
//        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mRgba, bmp);

        try {

            long start = System.currentTimeMillis();

//            // Assume the camera if front facing,
//            // mirror (horizontally flip) the preview
            Core.flip(mRgba, mRgba, 1);

            //FastCVWrapper cvWrapper = new FastCVWrapper();
            //cvWrapper.DetectFaces(mGray.getNativeObjAddr(), "haarcascade_frontalface_alt.xml");
            //cvWrapper.Blur(mRgba.getNativeObjAddr());

            if (mAbsoluteFaceSize == 0) {
                int height = mGray.rows();
                if (Math.round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                }
                mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
            }

            MatOfRect faces = new MatOfRect();

            if (mDetectorType == JAVA_DETECTOR) {
                if (mJavaDetector != null)
                    mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            }
            else if (mDetectorType == NATIVE_DETECTOR) {
                if (mNativeDetector != null)
                    mNativeDetector.detect(mGray, faces);
            }
            else {
                Log.e(LOG_TAG, "Detection method is not selected!");
            }

            Rect[] facesArray = faces.toArray();
            for (int i = 0; i < facesArray.length; i++)
                Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);


            mExecutionTime += (System.currentTimeMillis() - start);
            String msg = String.format("Executed for %d ms.", mExecutionTime / ++mFramesReceived);
            Log.d(LOG_TAG, msg);

            Imgproc.putText(mRgba, mCameraDirective, new Point(100, 100),
                    3, 1, mCameraFontColor, 2);

        } catch(Exception ex) {

            Log.d(LOG_TAG, ex.getMessage());
        }

        return mRgba;
    }

    private void writeToFile(File file, String data) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream);
        outputStreamWriter.write(data);
        outputStreamWriter.close();
        stream.close();
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(LOG_TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(LOG_TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }

}
