#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOG_TAG "native-lib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

void sendRgbaToJava(JNIEnv* env, jobject thiz, jbyte* rgbaData, int width, int height) {
    jclass cls = env->GetObjectClass(thiz);
    jmethodID updateMethod = env->GetMethodID(cls, "updateProcessedFrameRGBA", "([BII)V");
    if (!updateMethod) {
        LOGI("updateProcessedFrameRGBA method not found");
        return;
    }
    jbyteArray outArray = env->NewByteArray(width * height * 4);
    env->SetByteArrayRegion(outArray, 0, width * height * 4, rgbaData);
    env->CallVoidMethod(thiz, updateMethod, outArray, width, height);
    env->DeleteLocalRef(outArray);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_flamedgeviewer_MainActivity_processFrame(JNIEnv* env, jobject thiz, jbyteArray argbFrame, jint width, jint height, jint filterType) {
    jbyte* bytes = env->GetByteArrayElements(argbFrame, NULL);
    cv::Mat argbMat(height, width, CV_8UC4, (unsigned char*)bytes);
    cv::Mat rgbaMat, grayMat, blurMat, edgeMat;

    // Android stores BGRA so convert to RGBA
    cv::cvtColor(argbMat, rgbaMat, cv::COLOR_BGRA2RGBA);

    switch (filterType) {
        case 0: // Edge detection on grayscale
            cv::cvtColor(rgbaMat, grayMat, cv::COLOR_RGBA2GRAY);
            cv::Canny(grayMat, edgeMat, 80, 150);
            // Convert edges back to RGBA for displaying white edges
            cv::cvtColor(edgeMat, rgbaMat, cv::COLOR_GRAY2RGBA);
            break;
        case 1: // Grayscale pass through
            cv::cvtColor(rgbaMat, grayMat, cv::COLOR_RGBA2GRAY);
            cv::cvtColor(grayMat, rgbaMat, cv::COLOR_GRAY2RGBA);
            break;
        case 2: // Blur RGB image
            cv::GaussianBlur(rgbaMat, blurMat, cv::Size(9, 9), 0);
            rgbaMat = blurMat;
            break;
        case 3: // None - pass original RGBA through
            // No processing needed
            break;
        default:
            break;
    }

    sendRgbaToJava(env, thiz, (jbyte*)rgbaMat.data, width, height);

    env->ReleaseByteArrayElements(argbFrame, bytes, 0);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_flamedgeviewer_MainActivity_stringFromJNI(JNIEnv* env, jobject) {
    return env->NewStringUTF("Hello from C++");
}
