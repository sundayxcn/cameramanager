package sunday.sdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件工具类FileUtil
 */
public class FileUtil {
    public static final String TAG = "FileUtil";
    public static final String FILE_TEMP_PATH = "/sdcard/faceCheck/";

    //创建文件夹
    public static void createDir(String path) {
        File dir = new File(path);
        //创建临时照片文件夹
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    //创建新文件
    public static void createFile(File file) {
//        File file = new File(path);
        //创建临时照片文件夹
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] getByte(Context context, String fileName) throws IOException{
        InputStream assetsInputStream = context.getAssets().open(fileName);
        int available = assetsInputStream.available();
        byte[] data = new byte[available];
        assetsInputStream.read(data);
        assetsInputStream.close();
        return  data;
    }

    public static void copyBigDataToSD(InputStream inputStream,String strOutFileName) throws IOException {
        //Log.w(TAG, "start copy file " + strOutFileName);
        //File file = new File()
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        File file = new File(sdDir.toString() + "/mtcnn/");
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdDir.toString() + "/mtcnn/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            //计算MD5的时间和写入的时间是相同的，所以比较MD5没有意义
            //String md5 = MD5Util.getFileMD5(f);
            //Log.w(TAG, "file exists " + strOutFileName);
            f.delete();
            f.createNewFile();
            //return;
        }
        //long time = SystemClock.uptimeMillis();
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString() + "/mtcnn/" + strOutFileName);
        myInput = inputStream;
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
//        long endTime = SystemClock.uptimeMillis() - time;
//        Log.w(TAG,"write" + strOutFileName+ "用时" + endTime +"ms");
        Log.i(TAG,"write model to sd :" + strOutFileName);
    }

    //将bitmap写入文件
    public static void writeBitmapToFile(Bitmap bitmap, String filePath, String fileName) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();

        String dirName = filePath;

        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String Name = fileName + ".jpg";
        File file = new File(dir, Name);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(datas);
        fos.close();
    }


    public static void writeBitmap(Bitmap bitmap) throws IOException {
        String name = TimeUtil.Stamp2String(System.currentTimeMillis());
        writeBitmapToFile(bitmap,FILE_TEMP_PATH, name);
    }

}
