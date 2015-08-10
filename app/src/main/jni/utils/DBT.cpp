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

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeCreateObject
        (JNIEnv * jenv, jclass, jstring jFileName, jint faceSize)
{
    char cwd[4096]; // TODO: change to MAXPATHLEN
    if( getcwd(cwd, sizeof(cwd)) != NULL )
        return 0;

    jlong result = 0;
    const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
    String stdFileName(jnamestr);

    CascadeClassifier face_cascade;
    face_cascade.load(stdFileName);
    if( face_cascade.empty() )
        return -1;

    return result;
}
