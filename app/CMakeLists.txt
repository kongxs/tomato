# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(CMAKE_CXX_STANDARD 14)

# 设置dlib
include(src/main/cpp/dlib/cmake)


add_subdirectory(src/main/cpp/alignment)


# 设置native目录
set(NATIVE_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp)

include_directories(${JNI_LIBS_DIR}/includes)

set(JNI_LIBS_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp)


add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED
        src/main/cpp/native-lib.cpp
        src/main/cpp/matutil.cpp
        src/main/cpp/effective.cpp
        src/main/cpp/JniTracker.cpp
             # Provides a relative path to your source file(s).
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)

include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp)


add_library(libopencv_java4  # 库名字
        SHARED
        IMPORTED)
set_target_properties( libopencv_java4
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/libs/armeabi-v7a/libopencv_java4.so)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_GLAGS} -L/${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}")

target_link_libraries( # Specifies the target library.
                       native-lib
                        libopencv_java4
                        android
                        jnigraphics
                        seeta_fa_lib
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

# 指定release编译选项
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s -O3 -Wall")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s -O3 -Wall")