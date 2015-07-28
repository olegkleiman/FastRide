LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# OpenCV
#OPENCV_LIB_TYPE:=STATIC
#OPENCV_CAMERA_MODULES:=on
#OPENCV_INSTALL_MODULES:=off
include D:/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := fastcvUtils
LOCAL_SRC_FILES := cvUtils.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
