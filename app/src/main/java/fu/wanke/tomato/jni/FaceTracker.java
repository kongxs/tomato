package fu.wanke.tomato.jni;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import fu.wanke.tomato.camera.Camera2Helper;

public class FaceTracker {

    static {
        System.loadLibrary("native-lib");
    }

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    boolean isInitinal ;


    public Faces faces;

    private int screenW;
    private int screenH;


    public FaceTracker(final Camera2Helper helper) {

        mHandlerThread = new HandlerThread("track");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //子线程 耗时再久 也不会对其他地方 (如：opengl绘制线程) 产生影响
                synchronized (FaceTracker.this) {
                    //定位 线程中检测
                    faces = detector((byte[]) msg.obj, helper.getSize().getWidth(),
                            helper.getSize().getHeight());
                    faces.set(screenW,screenH);
                }
            }
        };

    }

    public void doDetector(byte[] data) {
        mHandler.removeMessages(11);

        //加入新的11号任务
        Message message = mHandler.obtainMessage(11);
        message.obj = data;
        mHandler.sendMessage(message);

    }

    public void initinal(String path) {
        isInitinal = false;
        init(path);
        isInitinal = true;

    }

    private native void init(String model);

    private native Faces detector(byte[] bytes, int previewWidth ,int previewHeight);

    public void set(int screenSurfaceWid, int screenSurfaceHeight) {
        this.screenW = screenSurfaceWid;
        this.screenH = screenSurfaceHeight;
    }
}
