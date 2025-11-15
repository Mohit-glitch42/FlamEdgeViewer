#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define TAG "native-lib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

extern "C" {
JNIEXPORT void JNICALL
Java_com_example_flamedgeviewer_MainActivity_processFrame(JNIEnv *env, jobject thiz, jbyteArray yPlane, jint width, jint height) {
    jbyte* bytes = env->GetByteArrayElements(yPlane, NULL);

    // Convert Y plane to OpenCV Mat (grayscale)
    cv::Mat yMat(height, width, CV_8UC1, (unsigned char*)bytes);

    // Apply Canny edge detection
    cv::Mat edges;
    cv::Canny(yMat, edges, 80, 150);

    LOGI("Edge image size: %dx%d, nonzero pixels: %d", edges.cols, edges.rows, cv::countNonZero(edges));

    env->ReleaseByteArrayElements(yPlane, bytes, 0);
}

JNIEXPORT jstring JNICALL
Java_com_example_flamedgeviewer_MainActivity_stringFromJNI(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("Hello from C!");
}
}
