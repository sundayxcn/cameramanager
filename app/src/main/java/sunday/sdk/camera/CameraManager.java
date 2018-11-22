package sunday.sdk.camera;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.hardware.Camera;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



/**
 * @author Sunzhongfei
 * @decription 将camera相关从activity中抽离出来，
 * @data 2018/11/6
 **/
public class CameraManager {
    private final int frameSkip;
    private final int degree;
    private Camera mCamera;
    private boolean isCameraFront;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceHolder.Callback mSurfaceHolderCB;
    private int mTargetWidth;
    private int mTargetHeight;
    private Context mContext;
    private PreviewRepertory mPreviewRepertory;
    private CustomPreviewCB mCustomPreviewCB = new CustomPreviewCB();
    private long previewID = 0;
    private YUV2Bitmap yuv2Bitmap;
    private Camera.Parameters mParameters;

    private CameraManager(@NonNull SurfaceView surfaceView,
                          PreviewRepertory previewRepertory,
                          Camera.Parameters parameters,
                          int degree,
                          int targetWidth,
                          int targetHeight,
                          int frameSkip) {
        mContext = surfaceView.getContext();
        yuv2Bitmap = new YUV2Bitmap(mContext);
        mSurfaceHolder = surfaceView.getHolder();
        mPreviewRepertory = previewRepertory;
        this.mParameters = parameters;
        mSurfaceHolderCB = new CustomSurfaceHolderCallBack();
        mSurfaceHolder.addCallback(mSurfaceHolderCB);

        this.degree = degree;
        mTargetWidth = targetWidth;
        mTargetHeight = targetHeight;
        this.frameSkip = frameSkip;


    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int widthNew, int heightNew) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = widthNew * 1f / width;
        float scaleHeight = heightNew * 1f / height;

        //
        Bitmap resizedBitmap = Bitmap.createBitmap(widthNew, heightNew, bitmap.getConfig());
        Canvas canvas = new Canvas(resizedBitmap);
        canvas.save();
        canvas.scale(scaleWidth, scaleHeight);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        //
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.restore();

        return resizedBitmap;
    }

    public void setPreviewRepertory(PreviewRepertory previewRepertory) {
        mPreviewRepertory = previewRepertory;
    }


    public void convertCamera() {
        if (isCameraFront) {
            initCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            initCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        isCameraFront = !isCameraFront;
    }

    public boolean isCameraFront() {
        return isCameraFront;
    }

    public void initCamera(int cameraId) {
        if (mCamera != null) {
            stopCamera();
        }
        mCamera = Camera.open(cameraId);
        if (mSurfaceHolder != null) {
            setCameraSetting(mCamera, mSurfaceHolder);
        }

    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setCameraSetting(Camera camera, SurfaceHolder holder) {
        try {
            //摄像头设置SurfaceHolder对象，把摄像头与SurfaceHolder进行绑定
            camera.setPreviewDisplay(holder);

            //调整系统相机拍照角度
            camera.setDisplayOrientation(degree);

            Camera.Parameters parameters = mParameters == null ? camera.getParameters() : mParameters;
//            List<String> focusModes = parameters.getSupportedFocusModes();
//            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//            }else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)){
//                camera.autoFocus(new Camera.AutoFocusCallback() {
//                    @Override
//                    public void onAutoFocus(boolean success, Camera camera) {
//
//                    }
//                });
//            }
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;
//            List<Camera.Size> list = camera.getParameters().getSupportedPreviewSizes();
//            //设置帧数的
//            parameters.setPreviewSize(320,240);
            camera.setParameters(parameters);
//            //设置获取帧数回调

            int size = width * height * 3 / 2;
            camera.addCallbackBuffer(new byte[size]);
            camera.setPreviewCallbackWithBuffer(mCustomPreviewCB);
            camera.setPreviewCallback(mCustomPreviewCB);

        } catch (Exception e) {

            if (camera != null) {
                camera.release();
            }
            e.printStackTrace();
        }
    }

    public void openCamera() {
        initCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        isCameraFront = true;
    }

    public void closeCamera() {
        stopCamera();
        mPreviewRepertory.clear();
    }

    public void autoFocus() {
        //mCamera.autoFocus(null);
    }

    //停止拍照并释放相机资源
    private void stopCamera() {
        if (mCamera != null) {
            //停止预览
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            //释放相机资源
            mCamera.release();
            mCamera = null;
        }

    }


    private void stopPreview() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
    }


    public static class Builder {
        private int degree = 90;
        private int targetWidth = 240;
        private int targetHeight = 320;
        private int frameSkip = 2;
        private SurfaceView surfaceView;
        private Camera.Parameters parameters;
        private PreviewRepertory previewRepertory;

        public Builder(@NonNull SurfaceView surfaceView, PreviewRepertory previewRepertory) {
            this.surfaceView = surfaceView;
            this.previewRepertory = previewRepertory;
        }

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
            this.frameSkip = frame;
            return this;
        }

        public CameraManager build() {
            return new CameraManager(
                    surfaceView,
                    previewRepertory,
                    parameters,
                    degree,
                    targetWidth,
                    targetHeight,
                    frameSkip);
        }
    }

    public static final class YUV2Bitmap {
        private RenderScript renderScript;
        private ScriptIntrinsicYuvToRGB scriptIntrinsicYuvToRGB;
        private Type.Builder yuvType, rgbaType;
        private Allocation in, out;


        public YUV2Bitmap(Context context) {
            renderScript = RenderScript.create(context);
            scriptIntrinsicYuvToRGB = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));
        }

        public Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
            if (yuvType == null) {
                yuvType = new Type.Builder(renderScript, Element.U8(renderScript)).setX(nv21.length);
                in = Allocation.createTyped(renderScript, yuvType.create(), Allocation.USAGE_SCRIPT);
                rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(width).setY(height);
                out = Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SCRIPT);
            }
            in.copyFrom(nv21);
            scriptIntrinsicYuvToRGB.setInput(in);
            scriptIntrinsicYuvToRGB.forEach(out);
            Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            out.copyTo(bmpout);
            return bmpout;
        }
    }

    public class CustomPreviewCB implements Camera.PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mPreviewRepertory != null) {
                previewID++;
                if (previewID % frameSkip == 0) {
                    camera.addCallbackBuffer(data);
                    int width = camera.getParameters().getPreviewSize().width;
                    int height = camera.getParameters().getPreviewSize().height;
                    Bitmap bitmap = yuv2Bitmap.nv21ToBitmap(data, width, height);
                    if (width != mTargetWidth || height != mTargetHeight) {
                        if (degree == 90 || degree == 180) {
                            bitmap = resizeBitmap(bitmap, mTargetHeight, mTargetWidth);
                        } else {
                            bitmap = resizeBitmap(bitmap, mTargetWidth, mTargetHeight);
                        }
                    }
                    Preview preview = new Preview(previewID, bitmap);
                    mPreviewRepertory.addPreview(preview);
                }
            }
        }
    }

    public class CustomSurfaceHolderCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            openCamera();
            mCamera.startPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            if (mCamera != null && surfaceHolder != null) {
                //先停止后预览
                mCamera.startPreview();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            //停止拍照
            //surfaceHolder.removeCallback(this);
            stopCamera();
        }
    }


}
