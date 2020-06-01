package fu.wanke.tomato.jni;

public class Init {

    static {
        System.loadLibrary("native-lib");
    }

    public static native String test();
}
