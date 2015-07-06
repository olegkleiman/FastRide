LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS := -llog
LOCAL_MODULE    := fastcvUtils
LOCAL_CFLAGS    := -DDEBUG $(LOCAL_CFLAGS)
LOCAL_C_INCLUDES += $(JNI_DIR)
LOCAL_SRC_FILES := FPSCounter.cpp FastCVWrapper.cpp

include $(BUILD_SHARED_LIBRARY)