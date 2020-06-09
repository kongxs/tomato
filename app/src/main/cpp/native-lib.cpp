#include <android/log.h>
#include <android/bitmap.h>
#include "matutil.h"
#include <jni.h>
#include <alignment/include/face_alignment.h>

using namespace cv;
using namespace std;


//人脸检测核心模型

CascadeClassifier faceCascade;//opencv
Ptr<seeta::FaceAlignment> faceAlignment;

#define TAG "opencvLogTesst"

#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR, TAG, FORMAT, ##__VA_ARGS__);


extern "C"
JNIEXPORT void JNICALL
Java_fu_wanke_tomato_jni_FaceTracker_init(JNIEnv *env, jobject thiz, jstring model,
        jstring seatPath) {
//    faceCascade.load("/storage/emulated/0/haarcascade_frontalface_alt2.xml");

    const char *path = env->GetStringUTFChars(model, JNI_FALSE);

    faceCascade.load(path);



    const char *seatPat = env->GetStringUTFChars(seatPath, JNI_FALSE);

    faceAlignment = makePtr<seeta::FaceAlignment>(seatPat);

    LOGE("dlib init success ...");
}

extern "C"
JNIEXPORT jobject JNICALL
Java_fu_wanke_tomato_jni_FaceTracker_detector(JNIEnv *env, jobject thiz, jbyteArray bytes,
        jint width ,jint height) {

    char *path1 = "/storage/emulated/0/me.jpg" ;

    jbyte *data = env->GetByteArrayElements(bytes, NULL);



    Mat src(height + height / 2, width, CV_8UC1, data);

    //颜色格式的转换 nv21->RGBA
    //将 nv21的yuv数据转成了rgba
    cvtColor(src, src, COLOR_YUV2RGBA_I420);
//    cvtColor(src, src, COLOR_YUV2RGBA_NV21);

    // 正在写的过程 退出了，导致文件丢失数据

//    if (camera_id == 1) {
        //前置摄像头，需要逆时针旋转90度
//        Point center(src.cols/2,src.rows/2); //旋转中心
//        double angle = 90.0;  //角度
//        double scale = 1.0;  //缩放系数
//        Mat rotMat = getRotationMatrix2D(center,angle,scale);
//        warpAffine(src,src,rotMat,src.size());

        transpose(src,src);
        flip(src, src, -1);
//    } else {
//        顺时针旋转90度
//        rotate(src, src, ROTATE_90_CLOCKWISE);
//    }

    Mat gray;
    //灰色
    cvtColor(src, gray, COLOR_RGBA2GRAY);
    //增强对比度 (直方图均衡)
    equalizeHist(gray, gray);

//    imwrite(path1 , src);

    std::vector<Rect> faces;



    faceCascade.detectMultiScale(gray, faces, 1.2, 5, 0, Size(30, 30));

    int x = -1;
    int y = -1;
    int wid = 0;
    int hei = 0;

    jclass pJclass = env->FindClass("fu/wanke/tomato/jni/Faces");

    jmethodID conMethodId = env->GetMethodID(pJclass, "<init>", "()V");
    jmethodID setMethodId = env->GetMethodID(pJclass, "set", "(IIIIII)V");

    jobject face = env->NewObject(pJclass, conMethodId);
    
    if (faces.size() > 0) {
        
        Rect &rect = faces[0];
        x = rect.x;
        y = rect.y;

        hei = rect.height;
        wid = rect.width;



        seeta::FacialLandmark points[5];
        seeta::ImageData imageData(gray.cols, gray.rows);
//
        imageData.data = gray.data;
        seeta::FaceInfo faceInfo;
        seeta::Rect bbox;

        bbox.x = x;
        bbox.y = y;
        bbox.width = width;
        bbox.height = height;

        faceInfo.bbox = bbox;
        //检测 人眼 等五个点
        faceAlignment->PointDetectLandmarks(imageData, faceInfo, points);

        jmethodID methodId = env->GetMethodID(pJclass, "addPoint", "(II)V");

        for (int i = 0; i < 5; ++i) {
            double d = points[i].x;
            double d1 = points[i].y;

            env->CallVoidMethod(face,methodId,(int)d,(int)d1);
        }
        
    }

//    jmethodID pJmethodId = env->GetStaticMethodID(pJclass, "create",
//                                                  "(IIIIII)Lfu/wanke/tomato/jni/Faces;");

    env->CallVoidMethod(face,setMethodId,x,y,wid,hei,src.cols,src.rows);

    return face;

}