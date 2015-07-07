#include <jni.h>
#include <android/log.h>
#include "FastCVWrapper.h"
#include "FPSCounter.h"

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

    FPSCounter *counter = new FPSCounter();
    counter->FrameTick();

    delete counter;
}