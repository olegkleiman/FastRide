#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <vector>

#include "FastCVWrapper.h"
#include "FPSCounter.h"

using namespace std;
using namespace cv;

#define CVWRAPPER_LOG_TAG    "fastcvWrapper"
#ifdef DEBUG

#define DPRINTF(...)  __android_log_print(ANDROID_LOG_DEBUG,CVWRAPPER_LOG_TAG,__VA_ARGS__)
#else
#define DPRINTF(...)   //noop
#endif
#define IPRINTF(...)  __android_log_print(ANDROID_LOG_INFO,CVWRAPPER_LOG_TAG,__VA_ARGS__)
#define EPRINTF(...)  __android_log_print(ANDROID_LOG_ERROR,CVWRAPPER_LOG_TAG,__VA_ARGS__)
#define WPRINTF(...)  __android_log_print(ANDROID_LOG_WARN,CVWRAPPER_LOG_TAG,__VA_ARGS__)

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

JNIEXPORT int JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_DetectFace
    (JNIEnv *je, jclass jc, jlong addrRgba, jstring face_cascade_name)
{
    CascadeClassifier face_cascade;
    vector<Rect> faces;

    const char *cascade_name = je->GetStringUTFChars(face_cascade_name, NULL);

    if( !face_cascade.load(cascade_name) ) {
        DPRINTF("Can not load cascade filter");
    }

    je->ReleaseStringUTFChars(face_cascade_name, cascade_name);

    Mat imgGray;

    cvtColor(addrRgba, imgGray, CV_BGR2GRAY );
    face_cascade.detectMultiScale(addrRgba, faces, 1.1, 2,
                                    0|CV_HAAR_SCALE_IMAGE, Size(30, 30) );
    int face_size = faces.size();
    if ( face_size > 0)
    {
        int Y = faces[face_size -1].y - faces[face_size -1].height / 2;
    }

    return 1;
}


JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_Blur
        (JNIEnv *env, jclass jc, jlong addrChannel, int smoothType, jobject bitmap)
{

//    jclass clazz = je->FindClass("org/opencv/core/mat");
//    jmethodID methidID = je->GetMethodID(clazz, "getNativeObjAddr", "()J");

    Mat& mChannel  = *(Mat*)addrChannel;
    int matType = mChannel.type();
    CV_DbgAssert( matType == CV_8UC1 || matType == CV_8UC3 || matType == CV_8UC4 );

    AndroidBitmapInfo infoSrc;
    CV_DbgAssert( AndroidBitmap_getInfo(env, bitmap, &infoSrc) >= 0 );

//    uint32_t height = infoSrc.height;
//    uint32_t width  = infoSrc.width;
//    void    *pixels = 0;
//   CV_DbgAssert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
//   CV_DbgAssert( pixels );

    int dims = mChannel.dims;
    CV_DbgAssert( dims == 2 && infoSrc.height == (uint32_t)mChannel.rows && infoSrc.width == (uint32_t)mChannel.cols );

    flip(mChannel, mChannel, 1);

    switch( smoothType ) {
        case 1:
            blur(mChannel, mChannel, Size(29, 29));
            break;

        case 2: {
            const int MEDIAN_BLUR_FILTER_SIZE = 7;
            medianBlur(mChannel, mChannel, MEDIAN_BLUR_FILTER_SIZE);
        }
        break;

        case 3: {
            const double SIGMA_X = 7.0;
            GaussianBlur(mChannel, mChannel, Size(9,9), SIGMA_X);
        }
        break;

        case 4: {
            const double SIGMA_COLOR = 9.0;
            const double SIGMA_SPACE = 9.0;

            // This filter does not work inplace!
            Mat tmp(infoSrc.height, infoSrc.width, CV_8UC1);

            CV_DbgAssert( infoSrc.format == ANDROID_BITMAP_FORMAT_RGBA_8888 );
            CV_DbgAssert( matType == CV_8UC1);

            //cvtColor(mChannel, tmp, COLOR_GRAY2RGBA);
            // The source should be 8-bit, 1 channel
            bilateralFilter(mChannel, tmp, 15, 80, 80);
        }
        break;

        default:
            break;
    }

    AndroidBitmap_unlockPixels(env, bitmap);

}

JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_FindFeatures
    (JNIEnv *je, jclass jc, jlong addrGray, jlong addrRgba)
{
    //DPRINTF("Inside FindFeatures");

    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> keypoints;

    flip(mGr, mGr, 1);

    Ptr<ORB> orbDetector = ORB::create(500);
    orbDetector->detect(mGr, keypoints);
    DescriptorExtractor  extractor;
    Mat descriptor;
    orbDetector->compute(mGr, keypoints, descriptor);
//    circle(*pMatRgb, Point(100,100), 10, Scalar(5,128,255,255));
//    for( size_t i = 0; i < v.size(); i++ ) {
//        circle(*pMatRgb, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(255,128,0,255));
//    }

    Ptr<FastFeatureDetector> fastFeatureDetector = FastFeatureDetector::create(10, true, FastFeatureDetector::TYPE_7_12);
    if( fastFeatureDetector.empty())
    {
        DPRINTF("Can not create detector or descriptor extractor or descriptor matcher of given types");
        return;
    }
    fastFeatureDetector->detect(mGr, keypoints);

    drawKeypoints(mGr, keypoints, mRgb);

//    int start_x = 0;
//    int end_x = mRgb.size().width;
//
//    for (vector<KeyPoint>::iterator i = keypoints.begin(); i != keypoints.end(); i++)
////    //for( unsigned int i = 0; i < keypoints.size(); i++ )
//    {
//        if (i->pt.x > start_x && i->pt.x < end_x)
//        {
//            const KeyPoint& kp = *i;//keypoints[i];
//            circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
//        }
//    }


    //DPRINTF("Finished FindFeatures");
}