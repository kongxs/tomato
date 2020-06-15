package fu.wanke.tomato;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class LocalAssetManager {

    public static String PATH_FACE_MODEL = "";
    public static String PATH_SEAT_MODEL = "";
    public static String PATH_DLIB_MODEL = "";

    public static void init(Context context) {

        PATH_FACE_MODEL = doInit(context , "haarcascade_frontalface_alt2.xml");
        PATH_SEAT_MODEL = doInit(context , "seeta_fa_v1.1.bin");
        PATH_DLIB_MODEL = doInit(context , "shape_predictor_68_face_landmarks.dat");

    }


    private static String doInit(Context context , String modelFile) {

        String model = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                modelFile;

        OpenGlUtils.copyAssets2SdCard(context , modelFile,
                model);

        return model;
    }



}
