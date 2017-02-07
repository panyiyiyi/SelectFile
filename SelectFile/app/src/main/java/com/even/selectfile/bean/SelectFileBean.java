package com.even.selectfile.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Class Discription :
 * Created User : Even
 * Created Time : 2017/01/19.
 */

public class SelectFileBean implements Serializable {
    private boolean isImage;//判断是图片还是视频
    private String file_path;//视频或图片的路径

//    public SelectFileBean() {
//        super();
//    }
//
//    public SelectFileBean(Parcel source) {
//        isImage = source.readByte() == 1 ? true : false;
//        this.file_path = source.readString();
//    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeByte((byte) (this.isImage ? 1 : 0));
//        dest.writeString(file_path);
//
//    }
//
//    public static final Creator<SelectFileBean> CREATOR = new Creator<SelectFileBean>() {
//        @Override
//        public SelectFileBean createFromParcel(Parcel source) {
//            return new SelectFileBean(source);
//        }
//
//        @Override
//        public SelectFileBean[] newArray(int size) {
//            return new SelectFileBean[size];
//        }
//    };
}
