package com.even.selectfile.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.even.selectfile.MainActivity;
import com.even.selectfile.R;
import com.even.selectfile.Utils.ImageLoader;
import com.even.selectfile.bean.SelectFileBean;

import java.util.ArrayList;

/**
 * Class Discription :
 * Created User : Even
 * Created Time : 2017/01/19.
 */

public class ImageThumbAdapter extends RecyclerView.Adapter<ImageThumbAdapter.ImageThumbHolder> {

    private ArrayList<SelectFileBean> imagelist;
    private LayoutInflater inflater;
    private onItemClickListener clickListener;
    private boolean isImage;

    public void setClickListener(onItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ImageThumbAdapter(Context context, ArrayList<SelectFileBean> allImage, boolean b) {
        inflater = LayoutInflater.from(context);
        this.imagelist = allImage;
        this.isImage = b;
    }

    @Override
    public ImageThumbHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = inflater.inflate(R.layout.item_squareimage, null);
        return new ImageThumbHolder(inflate);
    }

    @Override
    public void onBindViewHolder(final ImageThumbHolder holder, final int position) {
        if (isImage) {//这里显示图片的
            holder.image_video.setVisibility(View.GONE);
            if (position == 0) {//第一张图片跳转到拍照界面
                holder.image.setImageResource(R.mipmap.ic_camera_alt_black_24dp);
            } else {
                if (MainActivity.filelist.contains(imagelist.get(position - 1))) {
                    holder.image_select.setVisibility(View.VISIBLE);
                } else {
                    holder.image_select.setVisibility(View.GONE);
                }
                ImageLoader.getInstance().loadImage(imagelist.get(position - 1).getFile_path(), holder.image);
            }
        } else {//这里是显示视频的
            if (position == 0) {//第一张图片跳转到拍摄视频界面界面
                holder.image_video.setVisibility(View.GONE);
                holder.image.setImageResource(R.mipmap.ic_videocam_black_24dp);
            } else {
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(imagelist.get(position - 1).getFile_path(), MediaStore.Images.Thumbnails.MINI_KIND);
                holder.image.setImageBitmap(bitmap);
                holder.image_video.setVisibility(View.VISIBLE);
                if (MainActivity.filelist.contains(imagelist.get(position - 1))) {
                    holder.image_select.setVisibility(View.VISIBLE);
                } else holder.image_select.setVisibility(View.GONE);
            }

        }

        //点击事件不做处理
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickListener.onitemClick(holder.image_select, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return imagelist.size() + 1;
    }

    class ImageThumbHolder extends RecyclerView.ViewHolder {
        private ImageView image, image_select, image_video;

        public ImageThumbHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.item_squareimage_image);
            image_select = (ImageView) itemView.findViewById(R.id.item_squareimage_selectimage);
            image_video = (ImageView) itemView.findViewById(R.id.item_squareimage_videoimage);
        }
    }

    /**
     * 点击接口
     */
    public interface onItemClickListener {
        void onitemClick(View view, int position);
    }
}
