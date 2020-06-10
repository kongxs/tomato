//
// Created by  on 2020/6/10.
//

#ifndef TOMATO_JNITRACKER_H
#define TOMATO_JNITRACKER_H

#include <jni.h>
#include <alignment/include/face_alignment.h>
#include "matutil.h"
#include <android/log.h>



using namespace cv;
using namespace std;

class JniTracker {
public:

    JniTracker(JNIEnv *env) {

//        pJclass = env->FindClass("fu/wanke/tomato/jni/Faces");
        pJclass = static_cast<jclass>(env->NewGlobalRef(
                env->FindClass("fu/wanke/tomato/jni/Faces")));

        setMethodId = env->GetMethodID(pJclass, "set", "(IIIIII)V");
        methodId = env->GetMethodID(pJclass, "addPoint", "(II)V");

        conMethodId = env->GetMethodID(pJclass, "<init>", "()V");

        clearMethod = env->GetMethodID(pJclass, "clearPoint", "()V");

        face = env->NewGlobalRef(env->NewObject(pJclass, conMethodId));

    }

    void init(JNIEnv *env,const char *model, const char *seeta);

    jobject startDetector(JNIEnv *env,const Mat& image,
                       CV_OUT vector<Rect>& objects,const Mat& src,int width,int height);
private:
    CascadeClassifier faceCascade;
    Ptr<seeta::FaceAlignment> faceAlignment;
    jmethodID setMethodId;
    jmethodID methodId;
    jclass pJclass;
    jmethodID conMethodId;
    jmethodID clearMethod;
    jobject face;

};


#endif //TOMATO_JNITRACKER_H
