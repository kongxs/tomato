package fu.wanke.tomato.gls;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import fu.wanke.tomato.camera.CameraEngine;

public class CameraControl {


    private final GLRootSurfaceView mGlRootView;
    private final Context mContext;
    private CameraEngine cameraEngine;
    private GLRender glRender;
    private ScaleGestureDetector scaleGestureDetector;

    public CameraControl(Context context, GLRootSurfaceView glRootView) {
        this.mGlRootView=glRootView;
        this.mContext = context;
        init();
    }

    private void init() {


        mGlRootView.setEGLContextClientVersion(2);

        cameraEngine=new CameraEngine(mContext);

        cameraEngine.setRenderCallback(new RenderCallback() {
            @Override
            public void renderImmediately() {
                mGlRootView.requestRender();
            }
        });

        cameraEngine.setPreviewSizeChangedCallback(new PreviewSizeChangedCallback() {
            @Override
            public void updatePreviewSize(final int previewWidth, final int previewHeight) {
                //heheda
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if(!GlobalConfig.FULL_SCREEN)
                            mGlRootView.setAspectRatio(previewWidth,previewHeight);
//                        else glRender.getOrthoFilter().updateProjection(previewWidth,previewHeight);
//                        if(screenSizeChangedListener!=null){
//                            screenSizeChangedListener.updateScreenSize(glRootView.getWidth(),glRootView.getHeight());
//                        }
                    }
                });
            }
        });

        scaleGestureDetector=new ScaleGestureDetector(mContext, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor=detector.getScaleFactor();
                cameraEngine.requestZoom(scaleFactor-1.0f);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                //return true to enter onScale()
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });

        mGlRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean ret;
                scaleGestureDetector.onTouchEvent(event);
//                if (!scaleGestureDetector.isInProgress()){
//                    if(rootViewClickListener!=null && event.getAction()==MotionEvent.ACTION_UP)
//                        rootViewClickListener.onRootViewTouched(event);
//                }
                ret=event.getPointerCount()!=1;
                return ret;
            }
        });

        mGlRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scaleGestureDetector.isInProgress()) return;
                cameraEngine.focusCamera();
//                Log.d(TAG, "onClick: "+glRootView.getWidth()+" "+glRootView.getHeight());
//                if(rootViewClickListener!=null)
//                    rootViewClickListener.onRootViewClicked();
            }
        });
//        mGlRootView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (scaleGestureDetector.isInProgress()) return true;
//                Log.d(TAG, "onLongClick: ");
//                if(rootViewClickListener!=null)
//                    rootViewClickListener.onRootViewLongClicked();
//                return true;
//            }
//        });


        glRender=new GLRender(mContext,cameraEngine);
        mGlRootView.setRenderer(glRender);
        mGlRootView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGlRootView.setClickable(true);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
            mGlRootView.setPreserveEGLContextOnPause(true);
        }
        cameraEngine.setGlRender(glRender);

    }

    public void onPause(){
        mGlRootView.onPause();
        glRender.onPause();
    }

    public void onResume(){
        mGlRootView.onResume();
        glRender.onResume();
    }

    public void onDestroy() {
        glRender.onDestroy();
    }

    public void switchCamera() {
        cameraEngine.switchCamera();
    }


    public interface PreviewSizeChangedCallback{
        void updatePreviewSize(int previewWidth,int previewHeight);
    }

    public interface RenderCallback{
        void renderImmediately();
    }
}
