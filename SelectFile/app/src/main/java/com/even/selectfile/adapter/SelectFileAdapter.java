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
 * Class Discription :选择图片Adapter
 * Created User : Even
 * Created Time : 2017/01/19.
 */

public class SelectFileAdapter extends RecyclerView.Adapter<SelectFileAdapter.SelectFileHolder> {
    private ArrayList<SelectFileBean> filelist;
    private LayoutInflater inflater;
    private onItemClickListener onClick;

    public void setOnClick(onItemClickListener onClick) {
        this.onClick = onClick;
    }

    public SelectFileAdapter(Context context, ArrayList<SelectFileBean> list) {
        this.filelist = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public SelectFileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = inflater.inflate(R.layout.item_selectfile, null);
        return new SelectFileHolder(inflate);
    }

    @Override
    public void onBindViewHolder(SelectFileHolder holder, final int position) {
        String file_path = filelist.get(position).getFile_path();
        if (filelist.get(position).isImage()) {
            ImageLoader.getInstance().loadImage(file_path, holder.image);
            holder.image_video.setVisibility(View.GONE);
        } else {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file_path, MediaStore.Images.Thumbnails.MINI_KIND);
            holder.image.setImageBitmap(bitmap);
            holder.image_video.setVisibility(View.VISIBLE);
        }

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filelist.size();
    }

    class SelectFileHolder extends RecyclerView.ViewHolder {
        private ImageView image, image_video;

        public SelectFileHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.item_selectfile);
            image_video = (ImageView) itemView.findViewById(R.id.item_selectfile_video);
        }
    }

    public interface onItemClickListener {
        void onClick(int position);
    }
}
