#include <jni.h>

#include <time.h>
#include <fstream>

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/objdetect.hpp>
#include <vector>

#include "FastCVWrapper.h"
#include "FPSCounter.h"

#ifdef __ANDROID__

#include <android/log.h>
#include <android/bitmap.h>

#endif

using namespace std;
using namespace cv;

#define CVWRAPPER_LOG_TAG    "fastcvWrapper"
#ifdef _DEBUG

#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,CVWRAPPER_LOG_TAG,__VA_ARGS__)
#else
#define DPRINTF(...)   //noop
#endif
#define IPRINTF(...)  __android_log_print(ANDROID_LOG_INFO,CVWRAPPER_LOG_TAG,__VA_ARGS__)
#define EPRINTF(...)  __android_log_print(ANDROID_LOG_ERROR,CVWRAPPER_LOG_TAG,__VA_ARGS__)
#define WPRINTF(...)  __android_log_print(ANDROID_LOG_WARN,CVWRAPPER_LOG_TAG,__VA_ARGS__)


vector<KeyPoint> applyAKAZE(Mat& src);
vector<KeyPoint> applyORB(Mat& src);

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    DPRINTF("JNI_VERSION_1_4");
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_FrameTick
  (JNIEnv *je, jclass jc)
{
    DPRINTF("Inside JNI");

//    FPSCounter *counter = new FPSCounter();
//    counter->FrameTick();
//
//    delete counter;
}

JNIEXPORT int JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_DetectFaces
    (JNIEnv *env, jclass jc, jlong addrGray, jstring face_cascade_name)
{
    try {
        CascadeClassifier face_cascade;
        vector<Rect> faces;

        const char *cascade_name = env->GetStringUTFChars(face_cascade_name, NULL);
        ifstream f(cascade_name);
        if (!f.good()) {
            DPRINTF("Can not access cascade file");
            return 0;
        }

        if (!face_cascade.load(cascade_name)) {
            DPRINTF("Can not load cascade");
            return 0;
        }

        env->ReleaseStringUTFChars(face_cascade_name, cascade_name);

        Mat &mGrayChannel = *(Mat *)addrGray;

        flip(mGrayChannel, mGrayChannel, 1);

        face_cascade.detectMultiScale(mGrayChannel, faces, 1.1, 2,
                                      0 | CV_HAAR_SCALE_IMAGE, Size(30, 30));
        int face_size = faces.size();
        if (face_size > 0) {
            DPRINTF("Detected %d faces", face_size);
//            *((Mat *) faces) = Mat(RectFaces, true);

            for(int i = 0; i < face_size; i++) {
                Rect _rect = faces[i];

                rectangle(mGrayChannel, _rect,
                        Scalar(255, 255, 255),
                        1,8,0);

                Mat faceROI = mGrayChannel(faces[i]);
                vector<Rect> eyes;
            }

        }

        return face_size;

    } catch(Exception& e) {
        jclass je = env->FindClass("org/opencv/core/CvException");

        if(!je)
            je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    } catch( ... ) {
        jclass je = env->FindClass("org/opencv/core/Exception");
        env->ThrowNew(je, "Unknown exception in JNI:DetectFaces");
    }
}


JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_Blur
        (JNIEnv *env, jclass jc, jlong addrGray, jlong addrRbga, int smoothType, jobject bitmap)
{

    try {
//    jclass clazz = je->FindClass("org/opencv/core/mat");
//    jmethodID methidID = je->GetMethodID(clazz, "getNativeObjAddr", "()J");

        Mat &mGrayChannel = *(Mat *) addrGray;
        Mat &mRgbChannel = *(Mat *) addrRbga;

        int matType = mGrayChannel.type();
        CV_DbgAssert(matType == CV_8UC1 || matType == CV_8UC3 || matType == CV_8UC4);

#ifdef __ANDROID__
        AndroidBitmapInfo infoSrc;
        CV_DbgAssert(AndroidBitmap_getInfo(env, bitmap, &infoSrc) >= 0);
#endif

//    uint32_t height = infoSrc.height;
//    uint32_t width  = infoSrc.width;
//    void    *pixels = 0;
//   CV_DbgAssert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
//   CV_DbgAssert( pixels );

        int dims = mGrayChannel.dims;
        CV_DbgAssert(dims == 2
                     && infoSrc.height == (uint32_t) mGrayChannel.rows
                     && infoSrc.width == (uint32_t) mGrayChannel.cols);

        flip(mGrayChannel, mGrayChannel, 1);

        switch (smoothType) {
            case 1:
                blur(mGrayChannel, mGrayChannel, Size(29, 29));
                break;

            case 2: {
                const int MEDIAN_BLUR_FILTER_SIZE = 7;
                medianBlur(mGrayChannel, mGrayChannel, MEDIAN_BLUR_FILTER_SIZE);
            }
                break;

            case 3: {
                const double SIGMA_X = 7.0;
                GaussianBlur(mGrayChannel, mGrayChannel, Size(9, 9), SIGMA_X);
            }
                break;

            case 4: {
                const double SIGMA_COLOR = 9.0;
                const double SIGMA_SPACE = 9.0;

                // This filter does not work inplace!
                Mat tmp(mGrayChannel.rows, mGrayChannel.cols, CV_8UC1);

#ifdef __ANDROID__
                CV_DbgAssert(infoSrc.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
#endif

                CV_DbgAssert(matType == CV_8UC1);

                //cvtColor(mChannel, tmp, COLOR_GRAY2RGBA);
                // The source should be 8-bit, 1 channel
                bilateralFilter(mGrayChannel, tmp, 15, 80, 80);
                cvtColor(tmp, mRgbChannel, CV_GRAY2RGB);
            }
                break;

            default:
                break;
        }

        //AndroidBitmap_unlockPixels(env, bitmap);

    } catch(Exception e) {

        DPRINTF("nMatToBitmap catched cv::Exception: %s", e.what());

        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    }

}

JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_FindFeaturesORB
        (JNIEnv *je, jclass jc, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;

    flip(mGr, mGr, 1);

    vector<KeyPoint> keypoints = applyORB(mGr);
    drawKeypoints(mGr, keypoints, mRgb);

//    DescriptorExtractor  extractor;
//    Mat descriptor;
//    orbDetector->compute(mGr, keypoints, descriptor);


}

vector<KeyPoint> applyORB(Mat& src) {
    vector<KeyPoint> keypoints;

    Ptr<ORB> orbDetector = ORB::create(500);
    orbDetector->detect(src, keypoints);

    return keypoints;
}

JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_FindFeaturesKAZE
        (JNIEnv *je, jclass jc, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;

    clock_t start = clock();

    flip(mGr, mGr, 1);

    vector<KeyPoint> keypoints = applyAKAZE(mGr);
    drawKeypoints(mGr, keypoints, mRgb);

    clock_t end = clock();
    double elapsed = ((double)(end - start )) / CLOCKS_PER_SEC;
    DPRINTF("AKAZE performed in %d", elapsed);

}

vector<KeyPoint> applyAKAZE(Mat& src){
    vector<KeyPoint> keypoints;

    Ptr<AKAZE> akazeDetector = AKAZE::create(true, true);
    akazeDetector->detect(src, keypoints);

    return keypoints;
}

JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_FindFeaturesFAST
    (JNIEnv *je, jclass jc, jlong addrGray, jlong addrRgba)
{
    //DPRINTF("Inside FindFeatures");

    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> keypoints;

    flip(mGr, mGr, 1);

    Ptr<FastFeatureDetector> fastFeatureDetector = FastFeatureDetector::create(10, true, FastFeatureDetector::TYPE_7_12);
    if( fastFeatureDetector.empty())
    {
        DPRINTF("Can not create detector of type FAST");
        return;
    }
    fastFeatureDetector->detect(mGr, keypoints);

    drawKeypoints(mGr, keypoints, mRgb);

}