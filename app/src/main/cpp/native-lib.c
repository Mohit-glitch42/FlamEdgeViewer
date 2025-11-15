#include <jni.h>  // <-- This is required!

JNIEXPORT jstring JNICALL
Java_com_example_flamedgeviewer_MainActivity_stringFromJNI(JNIEnv* env, jobject thiz) {
    return (*env)->NewStringUTF(env, "Hello from C!");
}
