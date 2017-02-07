package com.even.selectfile.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

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

public class ImageThumbActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Activity mActivity;
    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagethumb);
        this.mActivity = this;
        recyclerView = (RecyclerView) findViewById(R.id.activity_imagethumb_recycler);
        initData();

    }

    private void initData() {
        FileHelper helper = FileHelper.getInstance();
        helper.init(this);
        final ArrayList<SelectFileBean> allImage = helper.getAllImage();//获取所有SD卡图片资源

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        ImageThumbAdapter adapter = new ImageThumbAdapter(this, allImage, true);
        recyclerView.setAdapter(adapter);

        adapter.setClickListener(new ImageThumbAdapter.onItemClickListener() {
            @Override
            public void onitemClick(View view, int position) {
                if (position == 0) {
                    filePath = FileUtil.initFilePath();
                    File file = new File(filePath);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(intent, DataUtil.IMAGE_REQUESTCODE);

                } else {
                    if (MainActivity.filelist.contains(allImage.get(position - 1))) {//表示已经添加
                        MainActivity.filelist.remove(allImage.get(position - 1));
                        view.setVisibility(View.GONE);
                    } else {
                        MainActivity.filelist.add(allImage.get(position - 1));
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == DataUtil.IMAGE_REQUESTCODE) {
            SelectFileBean bean = new SelectFileBean();
            bean.setImage(true);
            bean.setFile_path(filePath);
            MainActivity.filelist.add(bean);
        }

    }
}
