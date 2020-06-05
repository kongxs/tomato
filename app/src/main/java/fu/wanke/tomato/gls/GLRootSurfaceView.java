package fu.wanke.tomato.gls;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

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


    public void enableBeauty(final boolean enable) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                renderer.enableBeauty(enable);
            }
        });
    }
}
