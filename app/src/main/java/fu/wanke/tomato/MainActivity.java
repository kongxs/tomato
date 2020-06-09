package fu.wanke.tomato;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

import fu.wanke.tomato.gls.BoundingBoxView;
import fu.wanke.tomato.gls.GLRootSurfaceView;
import fu.wanke.tomato.jni.Faces;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setAttributes(params);

        init();
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
