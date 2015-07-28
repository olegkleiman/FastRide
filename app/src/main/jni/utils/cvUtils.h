#include <jni.h>

#ifndef _Included_com_maximum_fastride_CV_UTILS
#define _Included_com_maximum_fastride_CV_UTILS
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Method:    FindFeatures
 * Signature: ()II
 */
JNIEXPORT void JNICALL Java_com_maximum_fastride_CameraCVActivity_FindFeatures(JNIEnv*, jclass, jlong addrGray, jlong addrRgba);


#ifdef __cplusplus
}
#endif
#endif