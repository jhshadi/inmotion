cmake_minimum_required(VERSION 3.6)

add_library(inmotion STATIC src/main/cpp/InMotion_jni.cpp)

find_package(OpenCV 4.3.0 REQUIRED java)
target_link_libraries(inmotion ${OpenCV_LIBRARIES})

