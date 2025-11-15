#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOG_TAG "native-lib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// Helper to call Java updateProcessedFrame method
void sendEdgeToJava(JNIEnv* env, jobject thiz, jbyte* edgeData, int width, int height) {
    jclass cls = env->GetObjectClass(thiz);
    jmethodID updateMethod = env->GetMethodID(cls, "updateProcessedFrame", "([BII)V");
    if (!updateMethod) {
        LOGI("updateProcessedFrame method not found");
        return;
    }

    jbyteArray outArray = env->NewByteArray(width * height);
    env->SetByteArrayRegion(outArray, 0, width * height, edgeData);
    env->CallVoidMethod(thiz, updateMethod, outArray, width, height);
    env->DeleteLocalRef(outArray);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_flamedgeviewer_MainActivity_processFrame(JNIEnv* env, jobject thiz, jbyteArray yPlane, jint width, jint height) {
    jbyte* bytes = env->GetByteArrayElements(yPlane, NULL);

    cv::Mat yMat(height, width, CV_8UC1, (unsigned char*)bytes);
    cv::Mat edges;
    cv::Canny(yMat, edges, 80, 150);

    LOGI("Edge image size: %dx%d, nonzero pixels: %d", edges.cols, edges.rows, cv::countNonZero(edges));

    sendEdgeToJava(env, thiz, (jbyte*)edges.data, width, height);

    env->ReleaseByteArrayElements(yPlane, bytes, 0);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_flamedgeviewer_MainActivity_stringFromJNI(JNIEnv* env, jobject) {
    return env->NewStringUTF("Hello from C++");
}
