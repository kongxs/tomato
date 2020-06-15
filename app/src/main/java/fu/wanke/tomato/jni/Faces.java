package fu.wanke.tomato.jni;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Faces {

    private int x ;
    private int y;

    private int  width;
    private int height;
    private  int imgWidth  ;
    private int imgHeight;
    private int screenW;
    private int screenH;

    private List<Point> points = new ArrayList<>();
    private List<Point> dlibPoints = new ArrayList<>();


    public void set(int x , int y ,int  width ,int height , int imgWidth ,int imgHeight) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setImgHeight(imgHeight);
        setImgWidth(imgWidth);
    }

    public void  addPoint(int x, int y) {
        Point point = new Point(x, y);
        this.points.add(point);
    }

    public void  addDlibPoint(int x, int y) {
        Point point = new Point(x, y);
        this.dlibPoints.add(point);
    }

    public void clearPoint() {
        this.points.clear();
    }

    public List<Point> getDlibPoints() {
        return dlibPoints;
    }

    public List<Point> getPoints() {
        return points;
    }

    public int getScreenW() {
        return screenW;
    }

    public void setScreenW(int screenW) {
        this.screenW = screenW;
    }

    public int getScreenH() {
        return screenH;
    }

    public void setScreenH(int screenH) {
        this.screenH = screenH;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void set(int screenSurfaceWid, int screenSurfaceHeight) {
        this.screenW = screenSurfaceWid;
        this.screenH = screenSurfaceHeight;
    }

    @Override
    public String toString() {
        return "Faces{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", imgWidth=" + imgWidth +
                ", imgHeight=" + imgHeight +
                ", screenW=" + screenW +
                ", screenH=" + screenH +
                ", points=" + points.toString() +
                '}';
    }
}
