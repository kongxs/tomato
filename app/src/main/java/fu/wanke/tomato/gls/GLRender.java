package fu.wanke.tomato.gls;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import fu.wanke.tomato.OpenGlUtils;
import fu.wanke.tomato.camera.Camera2Helper;
import fu.wanke.tomato.filter.BeautifyFilter;
import fu.wanke.tomato.filter.CameraFilter;
import fu.wanke.tomato.filter.ScreenFilter;
import fu.wanke.tomato.jni.FaceTracker;
import fu.wanke.tomato.jni.Faces;

public class GLRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera2Helper.OnPreviewSizeListener, Camera2Helper.OnPreviewListener {

    private final Context mContext;
    private final GLRootSurfaceView glRenderView;
    private Camera2Helper camera2Helper;
    private int[] mTextures;
    private SurfaceTexture mSurfaceTexture;
    private CameraFilter cameraFilter;
    private ScreenFilter screenFilter;
    private float[] mtx = new float[16];
    private int mPreviewWdith;
    private int mPreviewHeight;

    private BeautifyFilter beaytyFilter;
    private int screenSurfaceWid;
    private int screenSurfaceHeight;
    private int screenX;
    private int screenY;
    private FaceTracker tracker;
    private GLRootSurfaceView.OnDetectorListener mListener;

    public GLRender(Context context , GLRootSurfaceView view) {
        this.mContext = context;
        this.glRenderView = view;

        camera2Helper = new Camera2Helper((Activity) glRenderView.getContext());

        tracker = new FaceTracker(camera2Helper);


        String modelFile = "haarcascade_frontalface_alt2.xml";
        String model = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                modelFile;

        OpenGlUtils.copyAssets2SdCard(context , modelFile,
                model);


        String seatFile = "seeta_fa_v1.1.bin";
        String seatPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                seatFile;
        OpenGlUtils.copyAssets2SdCard(context , seatFile,
                seatPath);
        tracker.initinal(model , seatPath);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        mTextures = new int[1];
        //创建一个纹理
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        //将纹理和离屏buffer绑定
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);

        mSurfaceTexture.setOnFrameAvailableListener(this);

        //使用fbo 将samplerExternalOES 输入到sampler2D中
        cameraFilter = new CameraFilter(glRenderView.getContext());
        //负责将图像绘制到屏幕上
        screenFilter = new ScreenFilter(glRenderView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        camera2Helper.setPreviewSizeListener(this);
        camera2Helper.setOnPreviewListener(this);
        //打开相机
        camera2Helper.openCamera(width, height, mSurfaceTexture);

        float scaleX = (float) mPreviewHeight / (float) width;
        float scaleY = (float) mPreviewWdith / (float) height;

        float max = Math.max(scaleX, scaleY);

        screenSurfaceWid = (int) (mPreviewHeight / max);
        screenSurfaceHeight = (int) (mPreviewWdith / max);
        screenX = width - (int) (mPreviewHeight / max);
        screenY = height - (int) (mPreviewWdith / max);

        //prepare 传如 绘制到屏幕上的宽 高 起始点的X坐标 起使点的Y坐标
        cameraFilter.prepare(screenSurfaceWid, screenSurfaceHeight, screenX, screenY);
        screenFilter.prepare(screenSurfaceWid, screenSurfaceHeight, screenX, screenY);

        tracker.set(screenSurfaceWid,screenSurfaceHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        int textureId;
        // 配置屏幕
        //清理屏幕 :告诉opengl 需要把屏幕清理成什么颜色
        GLES20.glClearColor(0, 0, 0, 0);
        //执行上一个：glClearColor配置的屏幕颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //更新获取一张图
        mSurfaceTexture.updateTexImage();

        mSurfaceTexture.getTransformMatrix(mtx);
        //cameraFiler需要一个矩阵，是Surface和我们手机屏幕的一个坐标之间的关系
        cameraFilter.setMatrix(mtx);

        textureId = cameraFilter.onDrawFrame(mTextures[0]);

        // other filter ...

        Faces faces = tracker.faces;
        if (faces != null) {
            mListener.onDetector(faces);
        }


        if (beaytyFilter != null) {
            textureId = beaytyFilter.onDrawFrame(textureId);
        }

        int id = screenFilter.onDrawFrame(textureId);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glRenderView.requestRender();
    }

    @Override
    public void onSize(int width, int height) {
        mPreviewWdith = width;
        mPreviewHeight = height;
    }

    @Override
    public void onPreviewFrame(byte[] data, int len) {
//        Logger.error("onPreviewFrame : " + len);
        tracker.doDetector(data);
    }

    public void onSurfaceDestory() {
        camera2Helper.closeCamera();
    }

    public void enableBeauty(boolean enable) {
        if (enable) {

            beaytyFilter = new BeautifyFilter(mContext);
            beaytyFilter.prepare(screenSurfaceWid, screenSurfaceHeight, screenX, screenY);

        } else {
            beaytyFilter.release();
            beaytyFilter = null;
        }
    }

    public void setOnDetectorListener(GLRootSurfaceView.OnDetectorListener listener) {
        this.mListener = listener;
    }

}
