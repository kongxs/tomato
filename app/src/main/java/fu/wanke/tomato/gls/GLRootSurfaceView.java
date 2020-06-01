package fu.wanke.tomato.gls;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


public class GLRootSurfaceView extends GLSurfaceView {


    private int surfaceWidth;
    private int surfaceHeight;
    private double surfaceRatio;
    private GLRender renderer;

    public GLRootSurfaceView(Context context) {
        this(context , null);
    }

    public GLRootSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        bringToFront();
//    }
//
//    @Override
//    public void onPause() {
//        // TODO Auto-generated method stub
//        super.onPause();
//        CameraInterface.getInstance().doStopCamera();
//    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        surfaceWidth = width;
        surfaceHeight = height;
        surfaceRatio=(double)surfaceWidth/surfaceHeight;
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        setLayoutParams(layoutParams);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == surfaceWidth || 0 == surfaceHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * surfaceRatio) {
                setMeasuredDimension(width, (int) (width / surfaceRatio));
            } else {
                setMeasuredDimension((int) (height * surfaceRatio), height);
            }
        }
    }


}
