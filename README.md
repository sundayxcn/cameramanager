# cameramanager
用于快速接入camera预览

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
   implementation 'com.github.sundayxcn:cameramanager:1.1'
}

```
```java
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
