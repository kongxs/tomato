#include <android/log.h>
#include <android/bitmap.h>
#include "matutil.h"
#include <jni.h>
#include <alignment/include/face_alignment.h>
#include "JniTracker.h"

using namespace cv;
using namespace std;


//人脸检测核心模型

CascadeClassifier faceCascade;//opencv
Ptr<seeta::FaceAlignment> faceAlignment;

JavaVM *g_javaVM;
JniTracker *tracker;


#define TAG "opencvLogTesst"

#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##__VA_ARGS__);

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {

    g_javaVM = vm;
    JNIEnv *env;
    vm->GetEnv((void **) &env, JNI_VERSION_1_6);

    tracker = new JniTracker(env);

    return JNI_VERSION_1_6;
}
    
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    
}

extern "C"
JNIEXPORT jlong JNICALL
Java_fu_wanke_tomato_jni_FaceTracker_init(JNIEnv *env, jobject thiz, jstring model,
        jstring seatPath) {
//    faceCascade.load("/storage/emulated/0/haarcascade_frontalface_alt2.xml");

    const char *path = env->GetStringUTFChars(model, JNI_FALSE);

    faceCascade.load(path);


    const char *seatPat = env->GetStringUTFChars(seatPath, JNI_FALSE);

    faceAlignment = makePtr<seeta::FaceAlignment>(seatPat);


    tracker->init(env,path,seatPat);

    LOGE("dlib init success ...");

    return reinterpret_cast<jlong>(tracker);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_fu_wanke_tomato_jni_FaceTracker_detector(JNIEnv *env, jobject thiz, jlong  self ,jbyteArray bytes,
        jint width ,jint height) {


    JniTracker *tracker = (JniTracker*)self;

    char *path1 = "/storage/emulated/0/me.jpg" ;

    jbyte *data = env->GetByteArrayElements(bytes, NULL);

    Mat src(height + height / 2, width, CV_8UC1, data);


    //颜色格式的转换 nv21->RGBA
    //将 nv21的yuv数据转成了rgba
    cvtColor(src, src, COLOR_YUV2RGBA_I420);
//    if (camera_id == 1) {
        //前置摄像头，需要逆时针旋转90度
        transpose(src,src);
        flip(src, src, -1);
//    } else {
//        顺时针旋转90度
//        rotate(src, src, ROTATE_90_CLOCKWISE);
//    }

//    Jpegcompress(src,src,40);

//    imwrite(path1,src);

    Mat gray;
    //灰色
    cvtColor(src, gray, COLOR_RGBA2GRAY);
    //增强对比度 (直方图均衡)
    equalizeHist(gray, gray);

//    imwrite(path1 , src);

    std::vector<Rect> faces;

    return  tracker->startDetector(env,gray, faces,src,width,height);

}