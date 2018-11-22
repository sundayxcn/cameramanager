package sunday.sdk.camerademo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import sunday.sdk.camera.CameraManager;
import sunday.sdk.camera.FacePreviewRepertory;
import sunday.sdk.camera.PreviewRepertory;

public class MainActivity extends AppCompatActivity {

    //预览图的仓库，已包含一个默认的仓库，可复写接口PreviewRepertory自定义
    private PreviewRepertory previewRepertory = new FacePreviewRepertory();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.camera);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CameraActivity.class));
            }
        });
    }


}
