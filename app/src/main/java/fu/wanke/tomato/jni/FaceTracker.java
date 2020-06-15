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
    private long native_ptr;


    public FaceTracker(final Camera2Helper helper) {

        mHandlerThread = new HandlerThread("track");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //子线程 耗时再久 也不会对其他地方 (如：opengl绘制线程) 产生影响
                synchronized (FaceTracker.this) {
                    //定位 线程中检测
                    faces = detector(native_ptr,(byte[]) msg.obj, helper.getSize().getWidth(),
                            helper.getSize().getHeight());
                    if (faces != null)
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

    public void initinal(String path , String seatPath,String dlibPath) {
        isInitinal = false;
        native_ptr = init(path, seatPath,dlibPath);
        isInitinal = true;

    }

    private native long init(String model,String seatPath,String dlibPath);

    private native Faces detector(long self ,byte[] bytes, int previewWidth ,int previewHeight);

    public void set(int screenSurfaceWid, int screenSurfaceHeight) {
        this.screenW = screenSurfaceWid;
        this.screenH = screenSurfaceHeight;
    }
}
