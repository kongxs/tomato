#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_fu_wanke_tomato_jni_Init_test(
        JNIEnv* env,
        jclass /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
