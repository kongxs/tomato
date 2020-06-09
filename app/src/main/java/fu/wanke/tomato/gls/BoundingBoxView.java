package fu.wanke.tomato.gls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import fu.wanke.tomato.jni.Faces;


public class BoundingBoxView extends SurfaceView implements SurfaceHolder.Callback {

    protected SurfaceHolder mSurfaceHolder;

    private Paint mPaint;
    private Paint mPaintYello;


    private boolean mIsCreated;

    public BoundingBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5f);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaintYello = new Paint(mPaint);
        mPaintYello.setColor(Color.YELLOW);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mIsCreated = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mIsCreated = false;
    }

    public void setResults(Faces faces) {

        if (!mIsCreated) {
            return;
        }

        Canvas canvas = mSurfaceHolder.lockCanvas();

        //清除掉上一次的画框。
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        canvas.drawColor(Color.TRANSPARENT);


        int x = faces.getX();
        int y = faces.getY();

        x = (int) ((float) x / faces.getImgWidth() * faces.getScreenW());
        y = (int) ((float) y / faces.getImgHeight() * faces.getScreenH());

        int right = (int) (x + (float)faces.getWidth() / faces.getImgWidth() * faces.getScreenW());
        int bottom = (int) (y + (float)faces.getHeight() / faces.getImgHeight() * faces.getScreenH());

        canvas.drawRect(x,y, right, bottom,mPaint);

        mSurfaceHolder.unlockCanvasAndPost(canvas);

    }

}
