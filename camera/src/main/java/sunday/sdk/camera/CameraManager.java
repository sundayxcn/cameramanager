package sunday.sdk.camera;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.hardware.Camera;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;


/**
 * @author Sunzhongfei
 * @decription 将camera相关从activity中抽离出来，
 * @data 2018/11/6
 **/
public class CameraManager {
    public static final String TAG = "CameraManager";
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
    private boolean isPreviewing;
    private PictureBitmapCallback mPictureBitmapCallback;
    private boolean isBitmapScaleForce;

    private CameraManager(@NonNull SurfaceView surfaceView,
                          PreviewRepertory previewRepertory,
                          Camera.Parameters parameters,
                          int degree,
                          int targetWidth,
                          int targetHeight,
                          int frameSkip,
                          boolean isBitmapScaleForce) {
        mContext = surfaceView.getContext();
        yuv2Bitmap = new YUV2Bitmap(mContext);
        mSurfaceHolder = surfaceView.getHolder();
        mPreviewRepertory = previewRepertory;
        this.mParameters = parameters;
        mSurfaceHolderCB = new CustomSurfaceHolderCallBack();
        mSurfaceHolder.addCallback(mSurfaceHolderCB);
        this.isBitmapScaleForce = isBitmapScaleForce;
        this.degree = degree;
        mTargetWidth = targetWidth;
        mTargetHeight = targetHeight;
        this.frameSkip = frameSkip;

    }

    public void takePicture(final PictureBitmapCallback pictureCallback) {
        mPictureBitmapCallback = pictureCallback;
        isPreviewing = false;
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                int width = camera.getParameters().getPictureSize().width;
                int height = camera.getParameters().getPictureSize().height;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap = resizeBitmap(bitmap, width, height);
                mPictureBitmapCallback.takeBitmap(bitmap);
            }
        });
    }


    public Bitmap resizeBitmap(
            Bitmap bitmap,
            int width,
            int height) {
        Matrix matrix = new Matrix();
        matrix.postRotate(360 - degree);
        if (isCameraFront) {
            //镜像
            matrix.postScale(-1, 1);
        }
        float scaleX = 1f;
        float scaleY = 1f;
        if (degree == 90 || degree == 180) {
            scaleX = (float) mTargetWidth / (float) height;
            scaleY = (float) mTargetHeight / (float) width;
        } else {
            scaleX = (float) mTargetWidth / (float) width;
            scaleY = (float) mTargetHeight / (float) height;
        }

        if (isBitmapScaleForce) {
            matrix.postScale(scaleX, scaleY);
        } else {
            float scale = scaleX < scaleY ? scaleX : scaleY;
            matrix.postScale(scale, scale);
        }
        bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                width,
                height,
                matrix,
                true);
        return bitmap;
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
        startPreview();
        isCameraFront = !isCameraFront;
    }

    public boolean isCameraFront() {
        return isCameraFront;
    }

    public void initCamera(int cameraId) {
        try {
            stopCamera();
            mCamera = Camera.open(cameraId);
            if (mSurfaceHolder != null) {
                setCameraSetting(mCamera, mSurfaceHolder);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCamera = null;
        }

    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setCameraSetting(Camera camera, SurfaceHolder holder) throws IOException {
        //摄像头设置SurfaceHolder对象，把摄像头与SurfaceHolder进行绑定
        camera.setPreviewDisplay(holder);

        //调整系统相机拍照角度
        camera.setDisplayOrientation(degree);

        Camera.Parameters parameters = mParameters == null ? camera.getParameters() : mParameters;
        List<Camera.Size> list = camera.getParameters().getSupportedPictureSizes();
        int length = list.size();
        Camera.Size pictureSize = list.get(length - 3);
        for (Camera.Size size : list) {
            if (size.width == mTargetWidth && size.height == mTargetHeight) {
                pictureSize.width = mTargetWidth;
                pictureSize.height = mTargetHeight;
            }
        }
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
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
    }

    public synchronized void openCamera() {
        int num = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int targetId = Camera.CameraInfo.CAMERA_FACING_BACK;
        for (int i = 0; i < num; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                targetId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                isCameraFront = true;
            }
        }
        initCamera(targetId);

    }

    public void closeCamera() {
        try {
            stopCamera();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mPreviewRepertory.clear();
        }

    }

    public void startPreview() {
        if (!isPreviewing && mCamera != null) {
            mCamera.startPreview();
        }
        isPreviewing = true;
    }

    public void autoFocus() {
        //mCamera.autoFocus(null);
    }

    //停止拍照并释放相机资源
    private void stopCamera() throws IOException {
        if (mCamera != null) {
            //停止预览
            isPreviewing = false;
            stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.setPreviewDisplay(null);

            //释放相机资源
            mCamera.release();
            mCamera = null;
        }

    }


    private void stopPreview() {
        isPreviewing = false;
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
    }

    public boolean isBitmapScaleForce() {
        return isBitmapScaleForce;
    }

    public void setBitmapScaleForce(Boolean force) {
        isBitmapScaleForce = force;
    }

    public interface PictureBitmapCallback {
        void takeBitmap(Bitmap bitmap);
    }

    public static class Builder {
        private int degree = 90;
        private int targetWidth = 240;
        private int targetHeight = 320;
        private int frameSkip = 2;
        private boolean isBitmapScaleForce;
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

        public CameraManager build() {
            return new CameraManager(
                    surfaceView,
                    previewRepertory,
                    parameters,
                    degree,
                    targetWidth,
                    targetHeight,
                    frameSkip,
                    isBitmapScaleForce);
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
            if (isPreviewing && mPreviewRepertory != null) {
                previewID++;
                if (previewID % frameSkip == 0) {
                    if (camera != null) {
                        camera.addCallbackBuffer(data);
                    }
                    int width = camera.getParameters().getPreviewSize().width;
                    int height = camera.getParameters().getPreviewSize().height;
                    Bitmap bitmap = yuv2Bitmap.nv21ToBitmap(data, width, height);
                    bitmap = resizeBitmap(bitmap, width, height);
                    Preview preview = new Preview(previewID, bitmap);
                    mPreviewRepertory.addPreview(preview);
                }
            }
        }
    }

    public class CustomSurfaceHolderCallBack implements SurfaceHolder.Callback {


        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "Start preview display[SURFACE-CREATED]");
            checkCamera();
            //startPreviewDisplay(holder);
        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "Restart preview display[SURFACE-CHANGED]");
            stopPreviewDisplay();
            startPreviewDisplay(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            closeCamera();
        }

        private void stopPreviewDisplay() {
            checkCamera();
            try {
                stopPreview();
            } catch (Exception e) {
                Log.e(TAG, "Error while STOP preview for camera", e);
            }
        }

        private void startPreviewDisplay(SurfaceHolder holder) {
            checkCamera();
            try {
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);
                    mCamera.setPreviewCallback(mCustomPreviewCB);
                    startPreview();
                }
            } catch (IOException e) {
                Log.e("sunday", "Error while START preview for camera", e);
            }
        }

        private void checkCamera() {
            if (mCamera == null) {
                Log.w(TAG, "mCamera == null,Camera must be set when start/stop preview");
                openCamera();
            }
        }

    }
}
