package com.even.selectfile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.even.selectfile.activity.GalleryActivity;
import com.even.selectfile.activity.ImageThumbActivity;
import com.even.selectfile.activity.VideoThumbActivity;
import com.even.selectfile.adapter.SelectFileAdapter;
import com.even.selectfile.bean.SelectFileBean;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button btn_image, btn_video;
    private RecyclerView recyclerView;
    public static ArrayList<SelectFileBean> filelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filelist = new ArrayList<>();
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_image = (Button) findViewById(R.id.activity_main_image);
        btn_video = (Button) findViewById(R.id.activity_main_video);
        recyclerView = (RecyclerView) findViewById(R.id.activity_main_recycler);

        btn_image.setOnClickListener(OnClick);
        btn_video.setOnClickListener(OnClick);

    }


    private Button.OnClickListener OnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.activity_main_image:
                    intent = new Intent(MainActivity.this, ImageThumbActivity.class);
                    startActivity(intent);
                    break;
                case R.id.activity_main_video:
                    intent = new Intent(MainActivity.this, VideoThumbActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        SelectFileAdapter adapter = new SelectFileAdapter(this, filelist);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        adapter.setOnClick(new SelectFileAdapter.onItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                intent.putExtra("selectfile", filelist);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

    }
}
