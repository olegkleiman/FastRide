//
// Created by Oleg Kleiman on 10-Aug-15.
//
#include <jni.h>

#ifndef FASTRIDE_DBT_H
#define FASTRIDE_DBT_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeCreateObject
        (JNIEnv *, jclass, jstring, jint);

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeSetFaceSize
        (JNIEnv * jenv, jclass, jlong thiz, jint faceSize);


JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeDetect
        (JNIEnv * jenv, jclass, jlong thiz, jint imageGray, jlong faces);

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeStart
        (JNIEnv * jenv, jclass, jlong thiz);

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeStop
        (JNIEnv * jenv, jclass, jlong thiz);

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeDestroyObject
        (JNIEnv * jenv, jclass, jlong thiz);

#ifdef __cplusplus
}
#endif

#endif //FASTRIDE_DBT_H
