# cameramanager
用于快速接入camera预览
## 注意事项
1. 在有预览界面的activity使用rxpermission申请camera权限在部分手机上权限弹窗弹不出来
2. camera的打开和释放跟着surfaceView走，不需要额外在onresume和onpause操作
3. 第一次申请权限创建cameramanager后必须要手动opencamera，因为surface的create在之前已经走过了。

## 导入方式
第一步：
工程根目录中build.gradle中仓库地址增加maven jitpack
```
allprojects {
    repositories {
        jcenter()
        repositories {
            maven { url 'https://www.jitpack.io' }
        }
    }
}
```
第二步：
模块build.gradle中引入
```
dependencies {
   implementation 'com.github.sundayxcn:cameramanager:1.9'
}

```
```java
    
    public static final int REQUEST_CODE_CAMERA = 999;
    private SurfaceView mSurfaceView;
    private CameraManager cameraManager;
    //预览图的仓库，已包含一个默认的仓库，可复写接口PreviewRepertory自定义
    private PreviewRepertory previewRepertory = new FacePreviewRepertory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mSurfaceView = findViewById(R.id.surface_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != 0) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            }else {
                cameraManager = new CameraManager.Builder(mSurfaceView, previewRepertory).build();
            }
        } else {
            cameraManager = new CameraManager.Builder(mSurfaceView, previewRepertory).build();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length > 0) {
            for (String permission : permissions) {
                if (permission.equals(Manifest.permission.CAMERA)) {
                    cameraManager = new CameraManager.Builder(mSurfaceView, previewRepertory).build();
                    cameraManager.openCamera();
                    cameraManager.startPreview();
                } else {
                    Toast.makeText(CameraActivity.this, "没有权限", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
```
## 可配置项
```java
        public Builder Parameters(Camera.Parameters parameters) {
            this.parameters = parameters;
            return this;
        }

        /**
         * @param degree camera的角度，默认为90，也就是竖向
         **/
        public Builder displayOrientation(int degree) {
            this.degree = degree;
            return this;
        }

        /**
         * @param width  输出bitmap的大小，默认为240
         * @param height 输出bitmap的大小，默认为320
         **/
        public Builder targetWidthHeight(int width, int height) {
            targetWidth = width;
            targetHeight = height;
            return this;
        }

        /**
         * @param frame 每几帧跳一帧，根据实际情况来设定，默认为2，两帧取一帧
         */
        public Builder frameSkip(int frame) {
            if (frame > 0) {
                this.frameSkip = frame;
            }
            return this;
        }

        /**
         * @param force 如果目标的宽高和camera输出的宽高不成比例，
         *              true则强制缩放
         *              false则按照最小比例缩放，默认为false
         * */
        public Builder BitmapScaleForce(boolean force){
            isBitmapScaleForce = force;
            return this;
        }
```
