package sunday.sdk.camera;

import android.graphics.Bitmap;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FacePreviewRepertory implements PreviewRepertory {
    public static final String TAG = "PreviewRepertory";
    private ConcurrentLinkedQueue<Preview> mPreviewQueue = new ConcurrentLinkedQueue<Preview>();

    @Override
    public void addPreview(Preview preview) {
        Bitmap bitmap = preview.getBitmap();
        if (mPreviewQueue.size() > 5) {
            mPreviewQueue.clear();
        }
        preview.setBitmap(bitmap);
        mPreviewQueue.offer(preview);
    }

    @Override
    public Preview getLastPreview() {
        return mPreviewQueue.poll();
    }

    @Override
    public void clear() {
        mPreviewQueue.clear();
    }
}
