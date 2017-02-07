package com.even.selectfile.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Class Discription :
 * Created User : Even
 * Created Time : 2017/01/20.
 */

public class GalleryViewPagerAdapter extends PagerAdapter {
    ArrayList<View> list;

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(list.get(position % getCount()));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(list.get(position % getCount()));
        return list.get(position % getCount());
    }

    public GalleryViewPagerAdapter() {
        super();
    }

    /**
     * 为adapter设置数据源
     */
    public void setDataList(ArrayList<View> bitmaplist) {
        this.list = bitmaplist;
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
