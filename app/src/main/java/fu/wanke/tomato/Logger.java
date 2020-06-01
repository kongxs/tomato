package fu.wanke.tomato;

import android.util.Log;

public class Logger {

    private static final String TAG = "tomato:Logger";

    private Logger() {
    }

    public static void error(String message) {
        Log.e(TAG , message);
    }

}
