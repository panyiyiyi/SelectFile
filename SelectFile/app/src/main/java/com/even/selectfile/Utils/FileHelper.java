package com.even.selectfile.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.even.selectfile.bean.SelectFileBean;

import java.util.ArrayList;

/**
 * Class Discription :查询手机中的图片和视频文件工具类
 * Created User : Even
 * Created Time : 2017/01/19.
 */

public class FileHelper {
    private static FileHelper instance;
    private ContentResolver cr;
    private ArrayList<SelectFileBean> imagelist;
    private ArrayList<SelectFileBean> videolist;

    /**
     * 单例模式
     *
     * @return
     */
    public static FileHelper getInstance() {

        if (instance == null) {
            synchronized (FileHelper.class) {
                if (instance == null)
                    instance = new FileHelper();
            }
        }
        return instance;
    }

    public void init(Context context) {
        if (context != null) {
            cr = context.getContentResolver();
        }
    }

    /**
     * 获取手机中的所有图片
     */
    public ArrayList<SelectFileBean> getAllImage() {
        if (imagelist != null)
            return imagelist;
        imagelist = new ArrayList<>();
        String[] project = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        Cursor query = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, project, null, null, null);

        if (query.moveToFirst()) {
            int dataColumn = query.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                SelectFileBean bean = new SelectFileBean();
                bean.setFile_path(query.getString(dataColumn));
                bean.setImage(true);
                imagelist.add(bean);
            } while (query.moveToNext());

        }

        return imagelist;
    }

    /**
     * 获取手机中所有的视频
     *
     * @return
     */
    public ArrayList<SelectFileBean> getAllVedio() {
        if (videolist != null)
            return videolist;
        videolist = new ArrayList<>();
        String[] project = new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA};
        Cursor cs = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, project, null, null, null);
        if (cs.moveToFirst()) {
            int dataColum = cs.getColumnIndex(MediaStore.Video.Media.DATA);
            do {
                SelectFileBean bean = new SelectFileBean();
                bean.setFile_path(cs.getString(dataColum));
                bean.setImage(false);
                videolist.add(bean);
            } while (cs.moveToNext());
        }
        return videolist;
    }


}
