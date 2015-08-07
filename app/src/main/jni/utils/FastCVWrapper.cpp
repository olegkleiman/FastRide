#include <jni.h>
#include <android/log.h>

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

    //cvtColor(addrRgba, img_gray, CV_BGR2GRAY );
//    face_cascade.detectMultiScale(addrRgba, faces, 1.1, 2,
//                                    0|CV_HAAR_SCALE_IMAGE, Size(30, 30) );
    int face_size = faces.size();
    if ( face_size > 0)
    {
        int Y = faces[face_size -1].y - faces[face_size -1].height / 2;
    }

    return 1;
}

JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_Blur
    (JNIEnv *je, jclass jc, jlong addrGray)
{
    DPRINTF("Inside Blur");

    const int MEDIAN_BLUR_FILTER_SIZE = 7;
    Mat& mGr  = *(Mat*)addrGray;
    medianBlur(mGr, mGr, MEDIAN_BLUR_FILTER_SIZE);
}


JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_FindFeatures
    (JNIEnv *je, jclass jc, jlong addrGray, jlong addrRgba)
{
    //DPRINTF("Inside FindFeatures");

    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> keypoints;

    Ptr<FastFeatureDetector> fastFeatureDetector = FastFeatureDetector::create(1, true, FastFeatureDetector::TYPE_9_16);
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