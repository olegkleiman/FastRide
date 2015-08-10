//
// Created by Oleg on 10-Aug-15.
//

#ifndef FASTRIDE_DBT_H
#define FASTRIDE_DBT_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_maximum_fastride_fastcv_DetectionBasedTracker_nativeCreateObject
        (JNIEnv *, jclass, jstring, jint);

#ifdef __cplusplus
}
#endif

#endif //FASTRIDE_DBT_H
