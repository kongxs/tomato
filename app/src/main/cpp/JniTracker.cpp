//
// Created by  on 2020/6/10.
//

#include "JniTracker.h"

void JniTracker::init(JNIEnv *env ,const char *model, const char *seeta,const char *dlibs) {

//    faceCascade.load(model);

    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    DetectionBasedTracker::Parameters detectorParams;
    //追踪器
    tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, detectorParams);

    faceAlignment = makePtr<seeta::FaceAlignment>(seeta);

    deserialize(dlibs) >> sp;
}

jobject JniTracker::startDetector(JNIEnv *env,const Mat& gray,
                               CV_OUT std::vector<Rect>& faces,const Mat& src,int width,int height) {

//    faceCascade.detectMultiScale(gray, faces, 1.2, 5, 0, Size(30, 30));

    tracker->process(gray);
    //拿到人脸坐标信息
    tracker->getObjects(faces);

    int x = -1;
    int y = -1;
    int wid = 0;
    int hei = 0;

//    env->CallVoidMethod(face,clearMethod);
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


        for (int i = 0; i < 5; ++i) {
            double d = points[i].x;
            double d1 = points[i].y;

            env->CallVoidMethod(face, methodId, (int) d, (int) d1);
        }

        dlib::rectangle rectangle;

        rectangle.set_left(faces[0].x);
        rectangle.set_top(faces[0].y);
        rectangle.set_right(faces[0].x + faces[0].width);
        rectangle.set_bottom(faces[0].y + faces[0].height);

        cv::Mat dlibs;
        cvtColor(src, dlibs, COLOR_RGBA2BGR);

        dlib::cv_image<bgr_pixel> dlibImg(dlibs);
        const full_object_detection &detection = sp(dlibImg, rectangle);

        if (detection.num_parts() == 68){
            for (int i = 0; i < 68; i++) {
                int x = detection.part(i).x();
                int y = detection.part(i).y();
                env->CallVoidMethod(face, addDlibPointMethodId, x, y);
            }
        }
    }
    env->CallVoidMethod(face,setMethodId,x,y,wid,hei,src.cols,src.rows);

    return face;
}