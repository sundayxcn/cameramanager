package sunday.sdk.cameraui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;

import sunday.sdk.camera.CameraManager;
import sunday.sdk.camera.FacePreviewRepertory;
import sunday.sdk.camera.PreviewRepertory;


/**
 * @author sunzhongfei
 * @decrption
 * @data 2018/11/22
 **/
public abstract class CameraUIActivity extends FragmentActivity {
    public static final int REQUEST_CODE_CAMERA = 999;
    public static final int REQUEST_CODE_ABLUM = 998;
    private SurfaceView mSurfaceView;
    private TextView mAlbumView;
    private TextView mCancelView;
    private ImageView mTakePicView;
    private ImageView mConvertCameraView;
    private ImageView mAlbumShowView;
    protected CameraManager mCameraManager;
    private ViewGroup mTakeSelect;
    private Button mTakeCancel;
    private Button mTakeFinish;
    protected PreviewRepertory previewRepertory = new FacePreviewRepertory();
    private Size mTargetSize;
    private Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mTargetSize = getTargetSize();
        setupViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != 0) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            }else {
                mCameraManager = generatorCameraManager();
            }
        } else {
            mCameraManager = generatorCameraManager();
        }
    }


    protected Size getTargetSize(){
        return new Size(480,640);
    }



    protected abstract void takeFinish(Bitmap bitmap);
    //protected abstract void takeCancel();

    private void setupViews(){
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.setFocusable(true);
        mTakeSelect = findViewById(R.id.take_pic_select);
        mTakePicView = findViewById(R.id.iv_take_pic);
        mTakeCancel = mTakeSelect.findViewById(R.id.take_cancel);
        mTakeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBitmap();
            }
        });
        mTakeFinish = mTakeSelect.findViewById(R.id.take_finish);
        mTakeFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeFinish(mBitmap);
            }
        });
        mTakePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = previewRepertory.getLastPreview().getBitmap();
                showBitmap(bitmap);
//                mCameraManager.takePicture(new CameraManager.PictureBitmapCallback() {
//                    @Override
//                    public void takeBitmap(Bitmap bitmap) {
//                        showBitmap(bitmap);
//                    }
//                });
            }
        });
        mConvertCameraView = findViewById(R.id.iv_convert_camera);
        mConvertCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraManager.convertCamera();
            }
        });
        mCancelView = findViewById(R.id.tv_cancel);
        mCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //takeCancel();
                finish();
            }
        });
        mAlbumView = findViewById(R.id.tv_choose_from_album);
        mAlbumView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_ABLUM);
            }
        });
        mAlbumShowView = findViewById(R.id.album_view);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length > 0) {
            for (String permission : permissions) {
                if (permission.equals(Manifest.permission.CAMERA)) {
                    mCameraManager = generatorCameraManager();
                    mCameraManager.openCamera();
                    mCameraManager.startPreview();
                } else {
                    Toast.makeText(CameraUIActivity.this, "没有权限", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }



    public void showBitmap(@NonNull Bitmap bitmap){
        mBitmap = bitmap;
        mTakeSelect.setVisibility(View.VISIBLE);
        mAlbumShowView.setVisibility(View.VISIBLE);
        mAlbumShowView.setImageBitmap(bitmap);
    }


    public void hideBitmap(){
        mTakeSelect.setVisibility(View.GONE);
        mAlbumShowView.setVisibility(View.GONE);
        mCameraManager.startPreview();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ABLUM &&
                resultCode == Activity.RESULT_OK &&
                data != null){
            Bitmap bitmap = decodeUri(data.getData());
            showBitmap(bitmap);
        }
    }

    private CameraManager generatorCameraManager(){
        return new CameraManager.Builder(mSurfaceView, previewRepertory).
                targetWidthHeight(mTargetSize.getWidth(), mTargetSize.getHeight()).
                build();
    }



    private Bitmap decodeUri(Uri uri) {
        Bitmap bitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        int scaleWidth = options.outWidth / mTargetSize.getWidth();
        if (scaleWidth < 1) {
            scaleWidth = 1;
        }

        // Decode with inSampleSize
        options = new BitmapFactory.Options();
        options.inSampleSize = scaleWidth;

        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }


        int degree = 0;
        ContentResolver cr = this.getContentResolver();
        Cursor cursor = cr.query(uri, null, null, null, null);// 根据Uri从数据库中找
        if (cursor != null) {
            cursor.moveToFirst();// 把游标移动到首位，因为这里的Uri是包含ID的所以是唯一的不需要循环找指向第一个就是了
            String orientation = cursor.getString(cursor.getColumnIndex("orientation"));// 获取旋转的角度

            if (orientation != null) {
                degree = Integer.parseInt(orientation);
            } else {
                degree = 0;
            }

            cursor.close();
        }

        if (degree != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree); /*翻转角度*/
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }

        return bitmap;
    }


}
