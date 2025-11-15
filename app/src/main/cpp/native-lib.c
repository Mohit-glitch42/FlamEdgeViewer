#include <jni.h>
#include <android/log.h>

#define TAG "native-lib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

// Called from Camera2Preview: sends Y-plane of each camera frame
JNIEXPORT void JNICALL
Java_com_example_flamedgeviewer_MainActivity_processFrame(JNIEnv *env, jobject thiz, jbyteArray yPlane, jint width, jint height) {
    jbyte* bytes = (*env)->GetByteArrayElements(env, yPlane, NULL);

    // Example: Just log the first pixel value
    if (bytes != NULL) {
        LOGI("First Y pixel: %d (frame size: %dx%d)", bytes[0] & 0xFF, width, height);
    }

    (*env)->ReleaseByteArrayElements(env, yPlane, bytes, 0);
}

// Existing function
JNIEXPORT jstring JNICALL
Java_com_example_flamedgeviewer_MainActivity_stringFromJNI(JNIEnv* env, jobject thiz) {
    return (*env)->NewStringUTF(env, "Hello from C!");
}
