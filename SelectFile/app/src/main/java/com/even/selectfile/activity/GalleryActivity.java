package com.even.selectfile.activity;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.even.selectfile.MainActivity;
import com.even.selectfile.R;
import com.even.selectfile.Utils.FileUtil;
import com.even.selectfile.adapter.GalleryViewPagerAdapter;
import com.even.selectfile.bean.SelectFileBean;
import com.even.selectfile.view.ZoomImageView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class Discription :
 * Created User : Even
 * Created Time : 2017/01/19.
 */

public class GalleryActivity extends AppCompatActivity {
    private ViewPager pager;
    private Button btn_delete;

    private int position;
    private ArrayList<SelectFileBean> filelist;
    private ArrayList<View> bitmaplist;//存放bitmap的集合
    private int currentposition;

    private GalleryViewPagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_gallery);
        initData();
        initView();
    }

    private void initData() {
        position = getIntent().getIntExtra("position", 0);
        filelist = (ArrayList<SelectFileBean>) getIntent().getSerializableExtra("selectfile");
        currentposition = position;
    }

    /**
     * 获取bitmap图片
     */
    private void getBitmap() {
        Bitmap bitmap = null;
        bitmaplist = new ArrayList<>();
        for (SelectFileBean bean : filelist) {
            if (bean.isImage()) {
                try {
                    bitmap = FileUtil.revitionImageSize(bean.getFile_path());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                bitmap = ThumbnailUtils.createVideoThumbnail(bean.getFile_path(), MediaStore.Images.Thumbnails.MINI_KIND);
            }
            initViewPagerlist(bitmap);
        }
    }

    /**
     * 将bitmap图片转换成view
     *
     * @param bitmap
     */
    private void initViewPagerlist(Bitmap bitmap) {
        ZoomImageView image = new ZoomImageView(this);
        image.setImageBitmap(bitmap);
        image.setBackgroundColor(0xff000000);
        image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        bitmaplist.add(image);
    }

    private void initView() {
        pager = (ViewPager) findViewById(R.id.activity_gallery_viewpager);
        btn_delete = (Button) findViewById(R.id.activity_gallery_delete);
        if (filelist != null && filelist.size() >= 0) {
            getBitmap();
        } else {
            Toast.makeText(this, "没有图片", Toast.LENGTH_SHORT).show();
        }

        adapter = new GalleryViewPagerAdapter();
        adapter.setDataList(bitmaplist);
        pager.setAdapter(adapter);
        pager.setCurrentItem(position);
        pager.addOnPageChangeListener(pagechangelistener);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.filelist.remove(MainActivity.filelist.get(currentposition));
                pager.removeAllViews();
                filelist.remove(currentposition);
                if (filelist != null && filelist.size() > 0) {
                    getBitmap();
                    adapter.setDataList(bitmaplist);
                    adapter.notifyDataSetChanged();
                } else {
                    onBack();
                }
            }
        });

    }

    /**
     * 结束当前activity
     */
    private void onBack() {
        finish();
        overridePendingTransition(R.anim.activity_into, R.anim.activity_out);

    }

    private ViewPager.OnPageChangeListener pagechangelistener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            currentposition = position;
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
