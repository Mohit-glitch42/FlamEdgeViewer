cmake_minimum_required(VERSION 3.4.1)

# Path to your OpenCV Android SDK jni folder
set(OpenCV_DIR "C:/Users/vaibh/OneDrive/Documents/opencv-4.9.0-android-sdk/OpenCV-android-sdk/sdk/native/jni")

# Find OpenCV package with required modules
find_package(OpenCV REQUIRED core imgproc)

# Include OpenCV headers
include_directories(${OpenCV_INCLUDE_DIRS})

# Define your native library with source files
add_library(native-lib SHARED src/main/cpp/native-lib.cpp)



# Link your native library against OpenCV and Android log libraries
target_link_libraries(native-lib ${OpenCV_LIBS} log android)
