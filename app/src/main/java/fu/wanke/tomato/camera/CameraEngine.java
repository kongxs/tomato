package fu.wanke.tomato.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fu.wanke.tomato.gls.CameraControl;
import fu.wanke.tomato.gls.GLRender;

public class CameraEngine implements Camera.PreviewCallback, SurfaceTexture.OnFrameAvailableListener, Camera.AutoFocusCallback {

    private int mChainIdx;
    private int frameWidth;
    private int frameHeight;

    private boolean cameraOpened;
    private final Context mContext;

    private int cameraId;
    private Camera camera;

    private int displayRotate;

    private static final double preferredRatio=16.0/9;

    private byte[] mBuffer;

    private FakeMat[] mFrameChain;




    private Camera.Parameters mParams;
    private Camera.Size  previewSize;

    private boolean mCameraFrameReady;

    private CameraControl.RenderCallback mRRenderCallback;
    private SurfaceTexture mSurfaceTexture;
    private CameraControl.PreviewSizeChangedCallback previewSizeChangedCallback;
    private GLRender glRender;
    private Thread mWorkerThread;
    private boolean mStopThread;

    private double lastZoomValueRec;
    private int lastZoomValue;

    public CameraEngine(Context context) {
        this.mContext = context;
        frameWidth=480; frameHeight=640;
        cameraOpened=false;

        mChainIdx = 0;
        mFrameChain=new FakeMat[2];
        mFrameChain[0]=new FakeMat();
        mFrameChain[1]=new FakeMat();

    }

    public void setTexture(int mTextureID){
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }



    public long doTextureUpdate(float[] mSTMatrix){
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        return mSurfaceTexture.getTimestamp();
    }


    public long doTextureUpdate(DirectDrawer mDirectDrawer) {

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();
        float[] mtx = new float[16];
        mSurfaceTexture.getTransformMatrix(mtx);
        mDirectDrawer.draw(mtx);

        return mSurfaceTexture.getTimestamp();
    }

    /**
     *
     * @param facingFront
     * @see {@link Camera.CameraInfo.CAMERA_FACING_FRONT } {@link Camera.CameraInfo.CAMERA_FACING_BACK }
     * @return
     */
    public boolean openCamera(boolean facingFront) {

        synchronized (this) {
            int facing = facingFront ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
            cameraId = getCameraId(facing);

            camera = Camera.open(cameraId);
            camera.setPreviewCallbackWithBuffer(this);
            initRotateDegree(cameraId);

            focusCamera();


            if (camera != null) {
                mParams = camera.getParameters();

                List<Camera.Size> supportedPictureSizesList = mParams.getSupportedPictureSizes();
                List<Camera.Size> supportedVideoSizesList = mParams.getSupportedVideoSizes();
                List<Camera.Size> supportedPreviewSizesList = mParams.getSupportedPreviewSizes();

                previewSize = choosePreferredSize(supportedPreviewSizesList, preferredRatio);
                Camera.Size photoSize = choosePreferredSize(supportedPictureSizesList, preferredRatio);

                frameHeight = previewSize.width;
                frameWidth = previewSize.height;

                mParams.setPreviewSize(frameHeight, frameWidth);
                mParams.setPictureSize(photoSize.width, photoSize.height);

                // ??
                int size = frameWidth * frameHeight;
                size = size * ImageFormat.getBitsPerPixel(mParams.getPreviewFormat()) / 8;
                if (mBuffer == null || mBuffer.length != size)
                    mBuffer = new byte[size];
                mFrameChain[0].init(size);
                mFrameChain[1].init(size);


                camera.addCallbackBuffer(mBuffer);
//                camera.setParameters(mParams);
                cameraOpened = true;
            }
        }
        return cameraOpened;
    }



    private static Camera.Size choosePreferredSize(List<Camera.Size> sizes,double aspectRatio) {
        List<Camera.Size> options = new ArrayList<>();
        for (Camera.Size option : sizes) {
            if(option.width==1280 && option.height==720)
                return option;
            if (Math.abs((int)(option.height * aspectRatio)-option.width)<10) {
                options.add(option);
            }
        }
        if (options.size() > 0) {
            return Collections.max(options, new CompareSizesByArea());
        } else {
            return sizes.get(sizes.size()-1);
        }
    }

    /**
     *
     * @param cameraId
     */
    void initRotateDegree(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        WindowManager wm = (WindowManager)
                mContext.getSystemService(Context.WINDOW_SERVICE);

        int rotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        displayRotate = (info.orientation - degrees + 360) % 360;
    }

    private int getCameraId(int facing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == facing) {
                return camIdx;
            }
        }
        return 0;
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (this) {
            mFrameChain[mChainIdx].putData(data);
            mCameraFrameReady = true;

            camera.addCallbackBuffer(mBuffer);
            this.notify();
        }
    }

    public void setRenderCallback(CameraControl.RenderCallback renderCallback) {
        this.mRRenderCallback = renderCallback;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mRRenderCallback != null) {
            mRRenderCallback.renderImmediately();
        }
    }

    public void setPreviewSizeChangedCallback(CameraControl.PreviewSizeChangedCallback callback) {
        this.previewSizeChangedCallback = callback;
    }

    public void setGlRender(GLRender glRender) {
        this.glRender = glRender;
    }

    public boolean isCameraOpened() {
        return cameraOpened;
    }

    public void startPreview(){
        lastZoomValueRec=lastZoomValue=0;
        if(camera!=null){
            try {
                camera.setPreviewTexture(mSurfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
            previewSizeChangedCallback.updatePreviewSize(frameWidth,frameHeight);
            camera.startPreview();

            mCameraFrameReady = false;
            mStopThread = false;
            mWorkerThread = new Thread(new CameraWorker());
            mWorkerThread.start();
        }
    }

    public void stopPreview(){
        synchronized (this) {
            if(camera!=null){
                mStopThread = true;
                synchronized (this) {
                    this.notify();
                }
                mWorkerThread =  null;
                camera.stopPreview();
            }
        }
    }

    public void releaseCamera() {
        synchronized (this) {
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
            cameraOpened = false;
        }
    }

    public void requestZoom(double zoomValue){
        synchronized (this) {
            if (camera != null) {
                Camera.Parameters p = camera.getParameters();
                if(p.isZoomSupported()){
                    lastZoomValueRec +=zoomValue;
                    lastZoomValueRec=Math.max(0,Math.min(lastZoomValueRec,1.0));
                    int curZoom= (int) (lastZoomValueRec*p.getMaxZoom());
                    if(Math.abs(curZoom-lastZoomValue)>=1){
                        lastZoomValue= curZoom;
                        p.setZoom(lastZoomValue);
                    }
                }else return;
                camera.setParameters(p);
            }
        }
    }

    public void focusCamera(){
        synchronized (this) {
            if (camera != null) {
                camera.cancelAutoFocus();
                camera.autoFocus(this);
            }
        }
    }

    @Override
    public void onAutoFocus(boolean b, Camera camera) {

    }


    static class CompareSizesByArea implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.width * lhs.height -
                    (long) rhs.width * rhs.height);
        }
    }

    private class CameraWorker implements Runnable {
        @Override
        public void run() {
            do {
                boolean hasFrame = false;
                synchronized (CameraEngine.this) {
                    try {
                        while (!mCameraFrameReady && !mStopThread) {
                            CameraEngine.this.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mCameraFrameReady) {
                        mChainIdx = 1 - mChainIdx;
                        mCameraFrameReady = false;
                        hasFrame = true;
                    }
                }

                if (!mStopThread && hasFrame) {
                    //processCameraFrame(frameHeight,frameWidth,mFrameChain[1 - mChainIdx].getFrame());
                }
            } while (!mStopThread);
        }
    }

    public class PreviewSize{
        private int width,height;

        public PreviewSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
