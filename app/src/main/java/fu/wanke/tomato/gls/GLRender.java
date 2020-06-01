package fu.wanke.tomato.gls;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import fu.wanke.tomato.Logger;
import fu.wanke.tomato.camera.CameraEngine;
import fu.wanke.tomato.camera.DirectDrawer;
import fu.wanke.tomato.filter.GPUVideoFilter;
import fu.wanke.tomato.filter.GPUVideoGrayscaleFilter;

public class GLRender implements GLSurfaceView.Renderer {

    private final Context mContext;
    private final CameraEngine cameraEngine;
    private final LinkedList<Runnable> mPostDrawTaskList;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean isCameraFacingFront ;
    private int mTextureID;
    private DirectDrawer directDrawer;

    private final LinkedList<Runnable> mRunOnDraw;

    private  GPUVideoFilter mFilter;

    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;

    public static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    public static float TEXTURE_NO_ROTATION[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };

    public GLRender(Context mContext, CameraEngine cameraEngine) {
        this.mContext = mContext;
        this.cameraEngine = cameraEngine;

        mFilter = new GPUVideoFilter();

        mPostDrawTaskList=new LinkedList<>();

        mRunOnDraw = new LinkedList<>();

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

//        if (!isCameraFacingFront) {
//            TEXTURE_NO_ROTATION = new float[]{
//                    1.0f, 1.0f,
//                    1.0f, 0.0f,
//                    0.0f, 1.0f,
//                    0.0f, 0.0f,
//            };
//        }


        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TEXTURE_NO_ROTATION);

        setFilter(new GPUVideoGrayscaleFilter());
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        this.surfaceWidth=width;
        this.surfaceHeight=height;
        GLES20.glViewport(0,0,width,height);
//        filterGroup.onFilterChanged(width,height);
//        fbo=FBO.newInstance().create(surfaceWidth,surfaceHeight);
        if(cameraEngine.isCameraOpened()){
            cameraEngine.stopPreview();
            cameraEngine.releaseCamera();
        }

        createTextureID();

        mFilter.init();
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(width, height);

        directDrawer = new DirectDrawer(mTextureID);

        cameraEngine.setTexture(mTextureID);

        cameraEngine.openCamera(isCameraFacingFront);
        cameraEngine.startPreview();
    }

    private void createTextureID() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        mTextureID = texture[0];
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        long timeStamp=cameraEngine.doTextureUpdate(directDrawer);
//        runPostDrawTasks();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(mRunOnDraw);
        mFilter.onDraw(mTextureID, mGLCubeBuffer, mGLTextureBuffer);
//        runAll(mRunOnDrawEnd);

    }

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    public void runPostDrawTasks() {
        while (!mPostDrawTaskList.isEmpty()) {
            mPostDrawTaskList.removeFirst().run();
        }
    }

    public void addPostDrawTask(final Runnable runnable) {
        synchronized (mPostDrawTaskList) {
            mPostDrawTaskList.addLast(runnable);
        }
    }

    public void setFilter(final GPUVideoFilter filter) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                final GPUVideoFilter oldFilter = mFilter;
                mFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                mFilter.init();
                GLES20.glUseProgram(mFilter.getProgram());
                mFilter.onOutputSizeChanged(surfaceWidth, surfaceHeight);
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }

    public void onPause(){
        if(cameraEngine.isCameraOpened()){
            cameraEngine.stopPreview();
            cameraEngine.releaseCamera();
        }
    }

    public void onResume() {
    }

    public void onDestroy(){
        if(cameraEngine.isCameraOpened()){
            cameraEngine.releaseCamera();
        }
    }
}
