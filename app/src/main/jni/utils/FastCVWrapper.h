/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_maximum_fastride_fastcv_FastCVWrapper */

#ifndef _Included_com_maximum_fastride_fastcv_FastCVWrapper
#define _Included_com_maximum_fastride_fastcv_FastCVWrapper
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_maximum_fastride_fastcv_FastCVWrapper
 * Method:    FrameTick
 * Signature: ()V
 Description of signatures: ([args]) [return type] -
                            () indicates a method taking no arguments
                            V indicates that it returns nothing
                            J - long
                            I - int
                            C - char
                            [ - array of thing following bracket

 */
JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_FrameTick
  (JNIEnv *, jclass);

/*
 * Class:     com_maximum_fastride_fastcv_FastCVWrapper
 * Method:    FrameTick
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_maximum_fastride_fastcv_FastCVWrapper_FindFeatures
(JNIEnv *, jclass, jlong addrGray, jlong addrRgba);

#ifdef __cplusplus
}
#endif
#endif
