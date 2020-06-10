//
// Created by 孔祥书 on 2020/6/10.
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
    void init(JNIEnv *env,const char *model, const char *seeta);

    jobject startDetector(JNIEnv *env,const Mat& image,
                       CV_OUT vector<Rect>& objects,const Mat& src,int width,int height);
private:
    CascadeClassifier faceCascade;
    Ptr<seeta::FaceAlignment> faceAlignment;
};


#endif //TOMATO_JNITRACKER_H
