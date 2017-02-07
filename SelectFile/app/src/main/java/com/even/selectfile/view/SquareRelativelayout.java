package com.even.selectfile.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Class Discription :自定义正方形图片显示
 * Created User : Even
 * Created Time : 2017/01/19.
 */

public class SquareRelativelayout extends RelativeLayout {


    public SquareRelativelayout(Context context) {
        super(context);
    }

    public SquareRelativelayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareRelativelayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置自己测量的结果
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        //测量子view
        int measuredWidth = getMeasuredWidth();

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = widthMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
