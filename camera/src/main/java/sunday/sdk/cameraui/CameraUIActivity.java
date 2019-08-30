package sunday.sdk.cameraui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;

import sunday.sdk.camera.CameraManager;
import sunday.sdk.camera.FacePreviewRepertory;
import sunday.sdk.camera.Preview;
import sunday.sdk.camera.PreviewRepertory;
import sunday.sdk.utils.BitmapUtil;
import sunday.sdk.utils.FileUtil;


/**
 * @author sunzhongfei
 * @decrption
 * @data 2018/11/22
 **/
public abstract class CameraUIActivity extends FragmentActivity {
    public static final int REQUEST_CODE_CAMERA = 999;
    public static final int REQUEST_CODE_ABLUM = 998;
    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    protected SurfaceView mSurfaceView;
    private TextView mAlbumView;
    private TextView mCancelView;
    private ImageView mTakePicView;
    private ImageView mConvertCameraView;
    private ImageView mAlbumShowView;
    protected CameraManager mCameraManager;
    private ViewGroup mTakeSelect;
    private Button mTakeCancel;
    private Button mTakeSave;
    private Button mTakeFinish;
    private Button mTakeRotate;
    protected PreviewRepertory previewRepertory = new FacePreviewRepertory();
    private Size mTargetSize;
    private Bitmap mBitmap;
    private ConstraintLayout mParentView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParentView = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.activity_camera,null,false);
        setContentView(mParentView);
        mTargetSize = getTargetSize();
        setupViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) != 0 ||
                    ContextCompat.checkSelfPermission(this, PERMISSIONS[1]) != 0||
                    ContextCompat.checkSelfPermission(this, PERMISSIONS[2]) != 0
                    ) {
                requestPermissions(PERMISSIONS, REQUEST_CODE_CAMERA);
            }else {
                mCameraManager = generatorCameraManager();
            }
        } else {
            mCameraManager = generatorCameraManager();
        }
    }


    public ConstraintLayout getParentView(){
        return mParentView;
    }

    public void resetUI(){
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
                resetUI();
            }
        });
        mTakeRotate = findViewById(R.id.take_rotate);
        mTakeRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBitmap = BitmapUtil.rotateBitmap(mBitmap,90);
                mAlbumShowView.setImageBitmap(mBitmap);
            }
        });
        mTakeSave = findViewById(R.id.take_save);
        mTakeSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FileUtil.writeBitmap(mBitmap);
                    showTips("文件已保存到根目录face_check中");
                } catch (IOException e) {
                    showTips("保存出错 errorMsg="+ e.getMessage());
                    e.printStackTrace();
                }
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

                Preview preview = previewRepertory.getLastPreview();
                if(preview == null){
                    Toast.makeText(CameraUIActivity.this,"仓库已空，请稍等",Toast.LENGTH_SHORT).show();
                }else {
                    Bitmap bitmap = preview.getBitmap();
                    mBitmap = bitmap;
                    showBitmap(bitmap);
                }
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
            for(int i = 0 ; i < permissions.length ;i++){
                if(grantResults[i] == 0){
                    if(permissions[i].equals(Manifest.permission.CAMERA)){
                        mCameraManager = generatorCameraManager();
                        mCameraManager.openCamera();
                        mCameraManager.startPreview();
                    }
                }

            }
        }
    }



    public void showBitmap(@NonNull Bitmap bitmap){
        mTakeSelect.setVisibility(View.VISIBLE);

        mAlbumShowView.setVisibility(View.VISIBLE);
        mAlbumShowView.setImageBitmap(bitmap);

        mAlbumView.setVisibility(View.GONE);

        mCameraManager.stopPreview();
    }


    public void hideBitmap(){
        mTakeSelect.setVisibility(View.GONE);

        mAlbumShowView.setVisibility(View.GONE);

        mAlbumView.setVisibility(View.VISIBLE);


        mCameraManager.startPreview();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ABLUM &&
                resultCode == Activity.RESULT_OK &&
                data != null){
            Bitmap bitmap = decodeUri(data.getData());
            mBitmap = bitmap;
            showBitmap(bitmap);
        }
    }

    protected CameraManager generatorCameraManager(){
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


    private void showTips(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }


}
