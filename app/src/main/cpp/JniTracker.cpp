//
// Created by 孔祥书 on 2020/6/10.
//

#include "JniTracker.h"

void JniTracker::init(JNIEnv *env ,const char *model, const char *seeta) {
    faceCascade.load(model);
    faceAlignment = makePtr<seeta::FaceAlignment>(seeta);
}

jobject JniTracker::startDetector(JNIEnv *env,const Mat& gray,
                               CV_OUT vector<Rect>& faces,const Mat& src,int width,int height) {

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
    env->CallVoidMethod(face,setMethodId,x,y,wid,hei,src.cols,src.rows);

    return face;
}