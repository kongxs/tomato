package fu.wanke.tomato;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import fu.wanke.tomato.gls.BoundingBoxView;
import fu.wanke.tomato.gls.GLRootSurfaceView;
import fu.wanke.tomato.jni.Faces;

public class MainActivity extends AppCompatActivity {


    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0 :
                    initAssets();
                    break;
                case 1:
                    onCreate();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        progressDialog = new ProgressDialog(MainActivity.this);
//        progressDialog
//                .show();
        handler.sendEmptyMessage(0);

//        onCreate();
    }

    private void onCreate() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setAttributes(params);

        init();
    }

    private void initAssets() {
        new Thread(){
            @Override
            public void run() {
                LocalAssetManager.init(MainActivity.this);
                handler.sendEmptyMessage(1);
            }
        }.start();

    }

    private void init() {
        final GLRootSurfaceView surfaceView = findViewById(R.id.gl_root_surface);

        SurfaceHolder holder = surfaceView.getHolder();

        final BoundingBoxView boundingBoxView = findViewById(R.id.boxView);

        surfaceView.setOnDetectorListener(new GLRootSurfaceView.OnDetectorListener() {
            @Override
            public void onDetector(Faces faces) {
                boundingBoxView.setResults(faces);
            }
        });

        CheckBox beauty = findViewById(R.id.beauty);
        beauty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                surfaceView.enableBeauty(isChecked);
            }
        });

        CheckBox bigeye = findViewById(R.id.bigeye);
        bigeye.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                surfaceView.enableBigEye(isChecked);
            }
        });

//        surfaceView.enableBeauty(true);
    }


}
