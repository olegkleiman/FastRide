package com.maximum.fastride;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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
import com.maximum.fastride.fastcv.FastCVCameraView;
import com.maximum.fastride.fastcv.FastCVWrapper;
import com.maximum.fastride.utils.Globals;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.rest.RESTException;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

//import org.opencv.features2d.KeyPoint;

public class CameraCVActivity extends Activity implements CvCameraViewListener2,
                                                          Camera.PictureCallback {

    private static final String LOG_TAG = "FR.CVCamera";

    private static final String STATE_CAMERA_INDEX = "cameraIndex";
    private static final String STATE_CURVE_FILTER_INDEX = "curveFilterIndex";
    private static final String STATE_MIXER_FILTER_INDEX = "mixerIndex";
    private static final String STATE_CONVOLUTION_FILTER_INDEX = "convolutionIndex";
    private static final String STATE_DETECTION_FILTER_INDEX = "detectionIndex";

    private FastCVCameraView mOpenCvCameraView;
    private int mCameraIndex;

    private boolean mIsCameraFrontFacing;
    private int mNumCameras;

    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;

    Scalar mCameraFontColor = new Scalar(255, 255, 255);
    String mCameraDirective;
    String mCameraDirective2;

    int mScreenWidth;
    int mScreenHeight;

    File mDetectorConfigFile;

    File mCascadeFile;
    CascadeClassifier mJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    FastCVWrapper mCVWrapper;

    private static final Object lock = new Object();

    boolean mRequestFrame = false;
    private void setRequestFrame() {
        synchronized (lock) {
            mRequestFrame = true;
        }
    }
    private boolean getRequestFrame() {
        synchronized (lock) {
            return mRequestFrame;
        }
    }
    private void resetRequestFrame() {
        synchronized (lock) {
            mRequestFrame = false;
        }
    }

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

//                        mJavaDetector =
//                                new CascadeClassifier(mCascadeFile.getAbsolutePath());
//                        if (mJavaDetector.empty()) {
//                            Log.e(LOG_TAG, "Failed to load cascade filter");
//                            mJavaDetector = null;
//                        } else
//                            Log.i(LOG_TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                        mCVWrapper = new FastCVWrapper(Globals.getCascadePath(getApplicationContext()));

                    } catch(IOException ex) {
                        Log.e(LOG_TAG, "Failed to load cascade. Exception: "  + ex.getMessage());

                    }

                    if( mOpenCvCameraView != null) {
                        mOpenCvCameraView.enableView();
                    }

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

        mOpenCvCameraView = (FastCVCameraView) findViewById(R.id.java_surface_view);
        //mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.native_surface_view);
        if( mOpenCvCameraView != null ) {
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);

            mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        }

        mCameraDirective = getString(R.string.camera_directive_1);
        mCameraDirective2 = getString(R.string.camera_directive_2);

        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;

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

    private void sampleFrame(Bitmap sampleBitmap) {

        if( sampleBitmap == null )
            return;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        sampleBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        new AsyncTask<InputStream, String, com.microsoft.projectoxford.face.contract.Face[]>(){

            // Progress dialog popped up when communicating with server.
            ProgressDialog mProgressDialog;

            @Override
            protected void onPreExecute() {
                mProgressDialog = ProgressDialog.show(CameraCVActivity.this,
                                                      getString(R.string.detection_send),
                                                      getString(R.string.detection_wait));
            }

            @Override
            protected void onPostExecute(Face[] result) {
                String strFormat = getString(R.string.detection_save);
                String msg = String.format(strFormat, result.length);
                Log.i(LOG_TAG, msg);

                try {
                    mProgressDialog.dismiss();

                    new MaterialDialog.Builder(CameraCVActivity.this)
                            .title(getString(R.string.detection_results))
                            .content(msg)
                            .positiveText(R.string.yes)
                            .negativeText(R.string.no)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    //finish();
                                }
                            })
                            .show();


                } catch(Exception ex) {
                    Log.e(LOG_TAG, ex.getMessage());
                }
            }

            @Override
            protected void onProgressUpdate(String... progress) {
                mProgressDialog.setMessage(progress[0]);
            }

            @Override
            protected Face[] doInBackground(InputStream... params) {

                // Get an instance of face service client to detect faces in image.
                FaceServiceClient faceServiceClient = new FaceServiceClient(getString(R.string.oxford_subscription_key));

                publishProgress("Detecting...");

                // Start detection.
                try {
                    return faceServiceClient.detect(
                            params[0],  /* Input stream of image to detect */
                            true,       /* Whether to analyzes facial landmarks */
                            false,       /* Whether to analyzes age */
                            false,       /* Whether to analyzes gender */
                            true);      /* Whether to analyzes head pose */
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());

                    publishProgress(e.getMessage());
                }

                return null;
            }

        }.execute(inputStream);
    }

    public void makeFrame(View view){

        mOpenCvCameraView.stopPreview();

        TextView txtStatus = (TextView)findViewById(R.id.detection_monitor);
        txtStatus.setText(getString(R.string.detection_center_desc));

        findViewById(R.id.detection_buttons_bar).setVisibility(View.VISIBLE);

    }

    public void sendToDetect(View view){

        // Dismiss buttons
        findViewById(R.id.detection_buttons_bar).setVisibility(View.GONE);

        // Restore status text
        TextView txtStatus = (TextView)findViewById(R.id.detection_monitor);
        txtStatus.setText(getString(R.string.detection_freeze));


        mOpenCvCameraView.takePicture(CameraCVActivity.this);
    }

    public void restoreFromSendToDetect(View view){

        // Restore camera frames processing
        mOpenCvCameraView.startPreview();

        // Dismiss buttons
        findViewById(R.id.detection_buttons_bar).setVisibility(View.GONE);

        // Restore status text
        TextView txtStatus = (TextView)findViewById(R.id.detection_monitor);
        txtStatus.setText(getString(R.string.detection_freeze));
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        BitmapFactory.Options options=new BitmapFactory.Options();
        Bitmap _bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        sampleFrame(_bitmap);
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

//        if( getRequestFrame() ) {
//
//            // onCameraFrame is called not on UI thread
//            // On other hand, we run FaceAPI on UI thread only in order
//            // to show progress dialog
//
//            try {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        //mOpenCvCameraView.stopNestedScroll();
//
//                        Bitmap _bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(mRgba, _bitmap);
//                        resetRequestFrame();
//                        sampleFrame(_bitmap);
//                    }
//                });
//
//            } catch(Exception ex) {
//                Log.e(LOG_TAG, ex.getMessage());
//            }
//        }

//        mGray.height();
//        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mRgba, bmp);

        try {

            long start = System.currentTimeMillis();

//            // Assume the camera if front facing,
//            // mirror (horizontally flip) the preview
            //Core.flip(mGray, mGray, 1);

            int nFaces = 0;
            try {

                nFaces = mCVWrapper.DetectFaces(mGray.getNativeObjAddr(), mCVWrapper.pathToCascade);
                //cvWrapper.Blur(mRgba.getNativeObjAddr());

//            if (mAbsoluteFaceSize == 0) {
//                int height = mGray.rows();
//                if (Math.round(height * mRelativeFaceSize) > 0) {
//                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
//                }
//                mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
//            }

//                MatOfRect faces = new MatOfRect();
//            if (mDetectorType == JAVA_DETECTOR) {
//                if (mJavaDetector != null)
//                    mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                            new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//            } else if (mDetectorType == NATIVE_DETECTOR) {
//                if (mNativeDetector != null)
//                    mNativeDetector.detect(mGray, faces);
//            }
//            else {
//                Log.e(LOG_TAG, "Detection method is not selected!");
//            }
//

            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.getMessage());
            }

            mExecutionTime += (System.currentTimeMillis() - start);
            String msg = String.format("Executed for %d ms.", mExecutionTime / ++mFramesReceived);
            Log.d(LOG_TAG, msg);

            Imgproc.putText(mGray, mCameraDirective, new Point(100, 80),
                    3, 1, mCameraFontColor, 2);
            if( nFaces > 0 ) {
                String _s = String.format(mCameraDirective2, nFaces);
                Imgproc.putText(mGray, _s, new Point(100, mScreenHeight - 180),
                        3, 1, mCameraFontColor, 2);
            }

        } catch(Exception ex) {

            Log.d(LOG_TAG, ex.getMessage());
        }

        return mGray;
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
