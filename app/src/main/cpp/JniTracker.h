//
// Created by  on 2020/6/10.
//

#ifndef TOMATO_JNITRACKER_H
#define TOMATO_JNITRACKER_H

#include <jni.h>
#include <alignment/include/face_alignment.h>
#include "matutil.h"
#include <android/log.h>

#include <dlib/opencv.h>
#include <dlib/image_processing.h>
#include <dlib/image_processing/frontal_face_detector.h>

using namespace dlib;
using namespace cv;
using namespace std;

class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter(Ptr<CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {
            CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects) {
            Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize,
                                       maxObjSize);
    }

    virtual ~CascadeDetectorAdapter() {
    }

private:
    CascadeDetectorAdapter();

    Ptr<CascadeClassifier> Detector;
};

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

        addDlibPointMethodId = env->GetMethodID(pJclass, "addDlibPoint", "(II)V");

    }

    void init(JNIEnv *env,const char *model, const char *seeta,const char *dlibs);

    jobject startDetector(JNIEnv *env,const Mat& image,
                       CV_OUT std::vector<Rect>& objects,const Mat& src,int width,int height);
private:
//    CascadeClassifier faceCascade;
    Ptr<DetectionBasedTracker> tracker;
    Ptr<seeta::FaceAlignment> faceAlignment;
    jmethodID setMethodId;
    jmethodID methodId;
    jclass pJclass;
    jmethodID conMethodId;
    jmethodID clearMethod;
    jmethodID addDlibPointMethodId;

    shape_predictor sp;

};


#endif //TOMATO_JNITRACKER_H
