#include <jni.h>

#include "DBT.h"

#include <time.h>
#include <unistd.h> // for getcwd()
#include <limits>
#include <string>
#include <vector>

#include <opencv2/core/core.hpp>
#include <opencv2/objdetect.hpp>

#ifdef __ANDROID__

#include <android/log.h>
#include <android/bitmap.h>

#endif

using namespace cv;
using namespace std;

class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector{
    public:
        CascadeDetectorAdapter(Ptr<CascadeClassifier> detector) :
                IDetector(),
                Detector(detector){
            CV_DbgAssert(detector);
        }

        void detect(const Mat& Image, vector<Rect> &objects){
            Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize, maxObjSize);
        }

        virtual ~CascadeDetectorAdapter() {

        }

    private:
        CascadeDetectorAdapter();
        Ptr<CascadeClassifier> Detector;
};

struct DetectorAggregator
{
    Ptr<CascadeDetectorAdapter> mainDetector;
    Ptr<CascadeDetectorAdapter> trackingDetector;

    Ptr<DetectionBasedTracker> tracker;

    DetectorAggregator(Ptr<CascadeDetectorAdapter>& _mainDetector,
                        Ptr<CascadeDetectorAdapter>& _trackingDetector) :
            mainDetector(_mainDetector),
            trackingDetector(_trackingDetector)
    {
        CV_DbgAssert(_mainDetector);
        CV_DbgAssert(_trackingDetector);

        DetectionBasedTracker::Parameters DetectorParams;
        tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, DetectorParams);
    }
};

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeCreateObject
        (JNIEnv *env, jclass, jstring jFileName, jint faceSize)
{
    char cwd[4096]; // TODO: change to MAXPATHLEN
    getcwd(cwd, sizeof(cwd));

    jlong result = 0;
    const char* jnamestr = env->GetStringUTFChars(jFileName, NULL);
    String stdFileName(jnamestr);

    try {
        Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
                makePtr<CascadeClassifier>(stdFileName));
        Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
                makePtr<CascadeClassifier>(stdFileName));

        result = (jlong) new DetectorAggregator(mainDetector, trackingDetector);
        if (faceSize > 0) {
            mainDetector->setMinObjectSize(Size(faceSize, faceSize));
        }
    }catch(Exception& e){
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je)
            je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    }

    return result;
}


JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeSetFaceSize
        (JNIEnv *env, jclass, jlong thiz, jint faceSize)
{
    try {
        if( faceSize > 0 )
            ((DetectorAggregator *)thiz)->mainDetector->setMinObjectSize(Size(faceSize, faceSize));

    } catch(Exception e){
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je)
            je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    }
}

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeDetect
        (JNIEnv * env, jclass, jlong thiz, jint imageGray, jlong faces)
{
    try{

        vector<Rect> rectFaces;
        ((DetectorAggregator *)thiz)->tracker->process(*(Mat *)imageGray);
        ((DetectorAggregator *)thiz)->tracker->getObjects(rectFaces);

        *((Mat *)faces) = Mat(rectFaces, true);

    } catch(Exception& e) {
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je)
            je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    }
}

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeStart
        (JNIEnv * env, jclass, jlong thiz)
{
    try{

        ((DetectorAggregator *)thiz)->tracker->run();

    } catch(Exception& e) {
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je)
            je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    }
}

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeStop
        (JNIEnv * env, jclass, jlong thiz)
{
    try{

        ((DetectorAggregator *)thiz)->tracker->stop();

    } catch(Exception& e) {
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je)
            je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    }
}

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeDestroyObject
        (JNIEnv * env, jclass, jlong thiz)
{
    try{

        ((DetectorAggregator *)thiz)->tracker->stop();
        delete (DetectorAggregator *)thiz;

    } catch(Exception& e) {
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je)
            je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
    }
}