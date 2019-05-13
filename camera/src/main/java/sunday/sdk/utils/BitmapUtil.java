package sunday.sdk.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BitmapUtil {

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


    public static Bitmap convertBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();

        //
        Bitmap convertBitmap = Bitmap.createBitmap(w, bitmap.getHeight(), bitmap.getConfig());

        Canvas canvas = new Canvas(convertBitmap);
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        matrix.postTranslate(w, 0);

        canvas.drawBitmap(bitmap, matrix, null);

        return convertBitmap;
    }




    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    //提取像素点
    public static byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }


    //bitmap旋转
    public static Bitmap rotateBitmap(Bitmap bmp, int rotate) {
        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.setRotate(rotate);

        Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        return nbmp2;
    }

}
