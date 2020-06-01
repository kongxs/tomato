package fu.wanke.tomato;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import fu.wanke.tomato.gls.CameraControl;
import fu.wanke.tomato.gls.GLRootSurfaceView;

public class MainActivity extends AppCompatActivity {

    private CameraControl cameraControl;

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
        GLRootSurfaceView surfaceView = findViewById(R.id.gl_root_surface);
        cameraControl = new CameraControl(this,surfaceView);

        findViewById(R.id.flip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraControl.switchCamera();
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(cameraControl!=null)
            cameraControl.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(cameraControl!=null)
            cameraControl.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(cameraControl!=null)
            cameraControl.onDestroy();
    }

}
