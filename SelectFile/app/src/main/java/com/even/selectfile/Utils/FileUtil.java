package com.even.selectfile.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class Discription :存放拍摄的图片或视频的路径
 * Created User : Even
 * Created Time : 2017/01/19.
 */

public class FileUtil {

    /**
     * 获得存放图片视频路径
     */

    public static String initFilePath() {
        String path;
        File sdfile;
        String filepath;
        boolean sdExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdExist) {
            path = Environment.getExternalStorageDirectory().getPath() + "/selectfile/file";
            sdfile = new File(path);

            if (sdfile.exists()) {
                filepath = path + "/" + System.currentTimeMillis() + ".jpg";
            } else {
                sdfile.mkdir();
                filepath = sdfile.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg";
            }
            return filepath;
        }
        return null;

    }

    /**
     * 存放视频的位置
     *
     * @return
     */
    public static String initVideoPath() {
        String path;
        File sdfile;
        String filepath;
        boolean sdExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdExist) {
            path = Environment.getExternalStorageDirectory().getPath() + "/selectfile/file";
            sdfile = new File(path);

            if (sdfile.exists()) {
                filepath = path + "/" + System.currentTimeMillis() + ".mp4";
            } else {
                sdfile.mkdir();
                filepath = sdfile.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp4";
            }
            return filepath;
        }
        return null;
    }

    /**
     * 将文件路径的图片转换成bitmap
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static Bitmap revitionImageSize(String path) throws IOException {
        if (path == null) {
            return null;
        }
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                new File(path)));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);
        in.close();
        int i = 0;
        Bitmap bitmap = null;
        while (true) {
            if ((options.outWidth >> i <= 1000)
                    && (options.outHeight >> i <= 1000)) {
                in = new BufferedInputStream(
                        new FileInputStream(new File(path)));
                options.inSampleSize = (int) Math.pow(2.0D, i);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeStream(in, null, options);
                break;
            }
            i += 1;
        }
        return bitmap;
    }
}
