package sunday.sdk.camerademo;

import android.graphics.Bitmap;

import android.widget.Toast;

import sunday.sdk.cameraui.CameraUIActivity;

/**
 * @author sunzhongfei
 * @decrption
 * @data 2018/12/19
 **/
public class CameraActivity extends CameraUIActivity {


    @Override
    protected void takeFinish(Bitmap bitmap) {
        Toast.makeText(this,"onClick Finish",Toast.LENGTH_SHORT).show();
    }

}
