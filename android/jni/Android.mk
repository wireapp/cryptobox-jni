LOCAL_PATH := $(call my-dir)

# libsodium
include $(CLEAR_VARS)
LOCAL_MODULE    := libsodium-prebuilt
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libsodium.so
include $(PREBUILT_SHARED_LIBRARY)

# libcryptobox
include $(CLEAR_VARS)
LOCAL_MODULE            := libcryptobox-prebuilt
LOCAL_SRC_FILES         := $(TARGET_ARCH_ABI)/libcryptobox.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

# jni bindings
include $(CLEAR_VARS)
LOCAL_MODULE           := cryptobox-jni
LOCAL_SRC_FILES        := ../../src/cryptobox-jni.c
LOCAL_SHARED_LIBRARIES := libsodium-prebuilt libcryptobox-prebuilt
LOCAL_LDLIBS           := -llog
LOCAL_CFLAGS           += -std=c99 -Wall
include $(BUILD_SHARED_LIBRARY)
