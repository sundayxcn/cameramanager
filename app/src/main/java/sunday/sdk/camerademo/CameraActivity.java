package sunday.sdk.camerademo;

import android.graphics.Bitmap;

import android.os.Bundle;
import android.widget.Toast;

import sunday.sdk.camera.CameraManager;
import sunday.sdk.cameraui.CameraUIActivity;

/**
 * @author sunzhongfei
 * @decrption
 * @data 2018/12/19
 **/
public class CameraActivity extends CameraUIActivity {


    @Override
    protected void takeFinish(Bitmap bitmap) {
        Toast.makeText(this, "onClick Finish", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected CameraManager generatorCameraManager() {
        return new CameraManager.Builder(mSurfaceView, previewRepertory).
                targetWidthHeight(320, 480).cameraFront(false).
                build();
    }
}
