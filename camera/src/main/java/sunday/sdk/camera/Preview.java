package sunday.sdk.camera;

import android.graphics.Bitmap;

public class Preview{
    private long id;
    private Bitmap bitmap;
    public Preview(long id,Bitmap bitmap){
        this.bitmap = bitmap;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


}
