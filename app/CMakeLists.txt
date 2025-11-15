cmake_minimum_required(VERSION 3.10.2)

project("com.example.flamedgeviewer")

# Adjust this path to your OpenCV SDK path
set(OpenCV_DIR "C:/Users/vaibh/OneDrive/Documents/opencv-4.9.0-android-sdk/OpenCV-android-sdk/sdk/native/jni")

find_package(OpenCV REQUIRED)

# Specify the relative path to the native source file from the app/ directory
add_library(native-lib SHARED src/main/cpp/native-lib.cpp)

target_include_directories(native-lib PRIVATE ${OpenCV_INCLUDE_DIRS})

find_library(log-lib log)

target_link_libraries(native-lib ${log-lib} ${OpenCV_LIBS})
