package com.even.selectfile.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.even.selectfile.MainActivity;
import com.even.selectfile.R;
import com.even.selectfile.Utils.DataUtil;
import com.even.selectfile.Utils.FileHelper;
import com.even.selectfile.Utils.FileUtil;
import com.even.selectfile.adapter.ImageThumbAdapter;
import com.even.selectfile.bean.SelectFileBean;

import java.io.File;
import java.util.ArrayList;

/**
 * Class Discription :
 * Created User : Even
 * Created Time : 2017/01/19.
 */

public class VideoThumbActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagethumb);
        recyclerView = (RecyclerView) findViewById(R.id.activity_imagethumb_recycler);
        initData();
    }

    private void initData() {
        FileHelper helper = FileHelper.getInstance();
        helper.init(this);
        final ArrayList<SelectFileBean> allVedio = helper.getAllVedio();//获取所有的视频

        ImageThumbAdapter adapter = new ImageThumbAdapter(this, allVedio, false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        adapter.setClickListener(new ImageThumbAdapter.onItemClickListener() {
            @Override
            public void onitemClick(View view, int position) {
                if (position == 0) {//跳转到摄像
                    filePath = FileUtil.initVideoPath();
                    File file = new File(filePath);
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(intent, DataUtil.VIDEO_REQUESTCODE);

                } else {
                    if (MainActivity.filelist.contains(allVedio.get(position - 1))) {
                        MainActivity.filelist.remove(allVedio.get(position - 1));
                        view.setVisibility(View.GONE);
                    } else {
                        MainActivity.filelist.add(allVedio.get(position - 1));
                        view.setVisibility(View.VISIBLE);
                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == DataUtil.VIDEO_REQUESTCODE) {
            SelectFileBean bean = new SelectFileBean();
            bean.setImage(false);
            bean.setFile_path(filePath);
            MainActivity.filelist.add(bean);
        }

    }
}
