package sunday.sdk.camera;

import android.graphics.Bitmap;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FacePreviewRepertory implements PreviewRepertory {
    public static final String TAG = "PreviewRepertory";
    public static final int MAX_CACHE = 5;
    private ConcurrentLinkedQueue<Preview> mPreviewQueue = new ConcurrentLinkedQueue<Preview>();

    @Override
    public void addPreview(Preview preview) {
        Bitmap bitmap = preview.getBitmap();
        if (mPreviewQueue.size() > MAX_CACHE) {
            mPreviewQueue.poll();
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
