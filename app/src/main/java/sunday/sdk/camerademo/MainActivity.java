package sunday.sdk.camerademo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import sunday.sdk.camera.CameraManager;
import sunday.sdk.camera.FacePreviewRepertory;
import sunday.sdk.camera.PreviewRepertory;

public class MainActivity extends AppCompatActivity {
    private SurfaceView mSurfaceView;
    private CameraManager cameraManager;
    //预览图的仓库，已包含一个默认的仓库，可复写接口PreviewRepertory自定义
    private PreviewRepertory previewRepertory = new FacePreviewRepertory();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.surface_view);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA).
                subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean aBoolean) {
                if(aBoolean){
                    cameraManager = new CameraManager.Builder(mSurfaceView,previewRepertory).build();
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraManager.openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraManager.closeCamera();
    }
}
