package sunday.sdk.camerademo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceView;
import android.widget.Toast;

import sunday.sdk.camera.CameraManager;
import sunday.sdk.camera.FacePreviewRepertory;
import sunday.sdk.camera.PreviewRepertory;


/**
 * @author sunzhongfei
 * @decrption
 * @data 2018/11/22
 **/
public class CameraActivity extends FragmentActivity {
    public static final int REQUEST_CODE_CAMERA = 999;
    private SurfaceView mSurfaceView;
    private CameraManager cameraManager;
    private PreviewRepertory previewRepertory = new FacePreviewRepertory();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mSurfaceView = findViewById(R.id.surface_view);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=0) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
        }else{
            cameraManager = new CameraManager.Builder(mSurfaceView,previewRepertory).build();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(permissions.length > 0){
            for(String permission : permissions){
                if(permission.equals(Manifest.permission.CAMERA)){
                    cameraManager = new CameraManager.Builder(mSurfaceView,previewRepertory).build();
                    cameraManager.openCamera();
                }else{
                    Toast.makeText(CameraActivity.this,"没有权限",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

}
