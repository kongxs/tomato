package fu.wanke.tomato.gls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import fu.wanke.tomato.jni.Faces;

public class GLRootSurfaceView extends GLSurfaceView {

    private GLRender renderer;

    public GLRootSurfaceView(Context context) {
        this(context,null);
    }

    public GLRootSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {

        setEGLContextClientVersion(2);
        renderer = new GLRender(context,this);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        renderer.onSurfaceDestory();
    }

    public void enableBeauty(final boolean enable) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.enableBeauty(enable);
            }
        });
    }

    public void enableBigEye(final boolean enable) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.enableBigEye(enable);
            }
        });
    }

    public void setOnDetectorListener(OnDetectorListener listener) {
        renderer.setOnDetectorListener(listener);
    }


    public interface OnDetectorListener {
        void onDetector(Faces faces);
    }


}
