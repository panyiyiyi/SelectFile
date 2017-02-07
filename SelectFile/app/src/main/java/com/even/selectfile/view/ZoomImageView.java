package com.even.selectfile.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Class Discription :自定义可缩放的imageView
 * Created User : Even
 * Created Time : 2017/02/06.
 */

public class ZoomImageView extends ImageView implements
        ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener {

    /**
     * 该参数用于判断某次触摸是否算是一个滑动操作
     */
    private static final int TOUCH_SLOP = 8;

    /**
     * 控件的宽度
     */
    private int mViewWidth;
    /**
     * 控件的高度
     */
    private int mViewHeight;
    /**
     * 用于判断图片是否已经初始化
     */
    private boolean mHasInit;
    /**
     * 最小缩放比例
     */
    private float mMinScale;
    /**
     * 初始化缩放比例
     */
    private float mInitScale;
    /**
     * 中等缩放比例
     */
    private float mMidScale;
    /**
     * 最大缩放比例
     */
    private float mMaxScale;
    /**
     * 图片矩阵
     */
    private Matrix mMatrix;
    /**
     * 用户多指触控监测器
     */
    private ScaleGestureDetector mScaleGestureDetector;
    /**
     * 用于手势操作监测器，主要进行双击操作的检测
     */
    private GestureDetector mGestureDetector;
    /**
     * 最后一次触摸操作的手指数量
     */
    private int mLastPointerCount;
    /**
     * 最后一次缩放位置的X轴方向中心
     */
    private float mLastScaleX;
    /**
     * 最后一次缩放位置的Y轴方向中心
     */
    private float mLastScaleY;
    /**
     * 用于判断图片是否可以移动
     */
    private boolean mPicCanMove;
    /**
     * 判断是否正在自动缩放状态，防止用户多次双击缩放，导致错乱
     */
    private boolean mIsAutoScaling;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMatrix = new Matrix();// 图片的矩阵
        setScaleType(ScaleType.MATRIX);// 在这里设置是屏蔽掉xml文件中设置的缩放模式

        mScaleGestureDetector = new ScaleGestureDetector(context, this);// 监听多指缩放手势
        setOnTouchListener(this);// 监听触摸事件

        mGestureDetector = new GestureDetector(context, new GestureDetector
                .SimpleOnGestureListener() {// 监听双击缩放
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mIsAutoScaling) {
                    return true;
                }

                float scaleX = e.getX();
                float scaleY = e.getY();

                float currentScale = getScale();

                if (mMinScale <= currentScale && currentScale < mMidScale) {
                    postDelayed(new AutoScaleRunnable(mMidScale, scaleX, scaleY), 0);
                } else if (mMidScale <= currentScale && currentScale < mMaxScale) {
                    postDelayed(new AutoScaleRunnable(mMaxScale, scaleX, scaleY), 0);
                } else {
                    postDelayed(new AutoScaleRunnable(mInitScale, scaleX, scaleY), 0);
                }

                return true;
            }

            /*
             * 如果当前图片是在一个Activity的ViewPager中打开的，单击可以关闭图片
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                ViewParent parent = getParent();
                if (parent instanceof ViewPager) {
                    Context context = ((ViewPager) parent).getContext();
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }

                return true;
            }
        });
    }

    @Override
    public void onGlobalLayout() {
        // 获取控件宽高
        mViewWidth = getWidth();
        mViewHeight = getHeight();

        // 初始化图片大小和位置
        if (!mHasInit) {
            // 获取图片宽高
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            int picWidth = drawable.getIntrinsicWidth();
            int picHeight = drawable.getIntrinsicHeight();

            // 设置初始、中等、最大缩放比例
            mInitScale = mViewWidth * 1.0f / picWidth;
            mMinScale = mInitScale * 0.8f;
            mMidScale = mInitScale * 2;
            mMaxScale = mInitScale * 4;

            // 设置图片移动到控件中心的偏移量
            int offsetX = mViewWidth / 2 - picWidth / 2;
            int offsetY = mViewHeight > picHeight * mInitScale
                    ? mViewHeight / 2 - picHeight / 2
                    : 0;// 长图片从头开始查看，所以偏移为0

            mMatrix.postTranslate(offsetX, offsetY);
            mMatrix.postScale(mInitScale, mInitScale, mViewWidth / 2,
                    mViewHeight > picHeight * mInitScale
                            ? mViewHeight / 2
                            : 0);// 长图片Y轴的缩放位置不在中心，而在顶部
            setImageMatrix(mMatrix);

            mHasInit = true;
        }
    }

    /**
     * 获得当前图片缩放比例
     *
     * @return 当前缩放比例
     */
    private float getScale() {
        float values[] = new float[9];
        mMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float currentScale = getScale();// 当前图片缩放比例
        float scaleFactor = detector.getScaleFactor();// 手势缩放因素

        if (getDrawable() == null) {
            return true;
        }

        /*
         * 控制缩放范围：
         * 放大手势——scaleFactor > 1.0f；
         * 缩小手势——scaleFactor < 1.0f；
         *
         * matrix.postScale()方法是按照已经缩放过的图片，再去进行一次缩放的。
         * 之前如果已经调用了postScale(scale, scale)，那么图片宽高就已经缩放了scale个系数，
         * 再调用postScale(scaleFactor, scaleFactor)，就会在scale系数的基础上缩放scaleFactor个系数，
         * 除以currentScale这个参数，就是为了将之前已经缩放过的scale个系数给抵消掉。
         */
        if ((currentScale < mMaxScale && scaleFactor > 1.0f)
                || (currentScale > mMinScale && scaleFactor < 1.0f)) {
            if (currentScale * scaleFactor < mMinScale) {
                // 除以currentScale，抵消上一次postScale()的缩放比例
                scaleFactor = mMinScale / currentScale;
            } else if (currentScale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale / currentScale;
            }

            mMatrix.postScale(scaleFactor, scaleFactor,
                    detector.getFocusX(), detector.getFocusY());

            drawableOffsetControl();// 控制图片位置

            setImageMatrix(mMatrix);
        }

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;// 这里return true才能识别缩放手势
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        mScaleGestureDetector.onTouchEvent(event);

        float scaleX = 0;// 多点触控X中心
        float scaleY = 0;// 多点触控Y中心
        int pointerCount = event.getPointerCount();

        // 计算X、Y轴的手势中心
        for (int i = 0; i < pointerCount; i++) {
            scaleX += event.getX(i);
            scaleY += event.getY(i);
        }
        scaleX = scaleX / pointerCount;
        scaleY = scaleY / pointerCount;

        // 手指数量发生改变，重新设置参数
        if (mLastPointerCount != pointerCount) {
            mLastPointerCount = pointerCount;
            mLastScaleX = scaleX;
            mLastScaleY = scaleY;
            mPicCanMove = false;
        }

        RectF matrixRectF = getMatrixRectF();
        float picWidth = matrixRectF.width();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // 兼容与ViewPager的冲突
                ViewParent parent = getParent();
                if (picWidth > mViewWidth + 0.01) {
                    if (parent instanceof ViewPager) {
                        parent.requestDisallowInterceptTouchEvent(true);
                        if (matrixRectF.right == mViewWidth
                                || matrixRectF.left == 0) {
                            parent.requestDisallowInterceptTouchEvent(false);
                        }
                    }
                }

                // 获取当前缩放中心和上一次缩放中心的差值
                float offsetX = scaleX - mLastScaleX;
                float offsetY = scaleY - mLastScaleY;

                if (!mPicCanMove) {
                    mPicCanMove = isMoveAction(offsetX, offsetY);
                } else {
                    if (getDrawable() != null) {
                        // 图片宽度小于控件宽度，X轴方向不移动
                        if (matrixRectF.width() < mViewWidth) {
                            offsetX = 0;
                        }
                        // 图片高度小于控件高度，Y轴方向不移动
                        if (matrixRectF.height() < mViewHeight) {
                            offsetY = 0;
                        }
                    }

                    mMatrix.postTranslate(offsetX, offsetY);
                    drawableOffsetControl();// 移动图片时的边界控制

                    setImageMatrix(mMatrix);
                }

                // 更新最后一次手势的中心
                mLastScaleX = scaleX;
                mLastScaleY = scaleY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                // 这里放大了一个肉眼看不出的数值：
                //
                // 在ACTION_MOVE中，如果图片的left或者right与控件边界相等，则不能移动图片，
                // 而是响应ViewPager的操作，这不是下一次操作所希望的；
                //
                // 缩放一个极小的数值，让图片的left、right与控件边界不等，使得可以重新进行移动图片。
                mMatrix.postScale(1.00001f, 1.000001f, mViewWidth / 2, mViewHeight / 2);
                setImageMatrix(mMatrix);
                break;
        }

        return true;
    }

    /**
     * 判断一次手势中心的偏移是否已经满足MotionEvent.ACTION_MOVE的操作
     *
     * @param offsetX 手势中心的X轴方向偏移量
     * @param offsetY 手势中心的Y轴方向偏移量
     * @return 如果算是一次MOVE操作，返回true，否则返回false
     */
    private boolean isMoveAction(float offsetX, float offsetY) {
        return Math.sqrt(offsetX * offsetX + offsetY * offsetY) > TOUCH_SLOP;
    }

    /**
     * 设置图片的偏移量，用于图片边界的控制，防止图片和控件之间出现白边
     */
    private void drawableOffsetControl() {
        RectF matrixRectF = getMatrixRectF();

        // 图片宽高
        float picWidth = matrixRectF.width();
        float picHeight = matrixRectF.height();

        // 图片偏移量
        float offsetX = 0;
        float offsetY = 0;

        // 图片X轴方向上的偏移量
        if (picWidth >= mViewWidth) {// 图片宽度大于控件宽度
            if (matrixRectF.left > 0) {// 左边留有空白
                offsetX = -matrixRectF.left;
            }

            if (matrixRectF.right < mViewWidth) {// 右边留有空白
                offsetX = mViewWidth - matrixRectF.right;
            }
        } else {// 图片宽度小于控件宽度，居中显示
            offsetX = mViewWidth / 2 - matrixRectF.right + picWidth / 2;
        }

        if (picHeight >= mViewHeight) {// 图片高度大于控件高度
            if (matrixRectF.top > 0) {// 上边留有空白
                offsetY = -matrixRectF.top;
            }

            if (matrixRectF.bottom < mViewHeight) {// 下边留有空白
                offsetY = mViewHeight - matrixRectF.bottom;
            }
        } else {// 图片高度小于控件高度，居中显示
            offsetY = mViewHeight / 2 - matrixRectF.bottom + picHeight / 2;
        }

        mMatrix.postTranslate(offsetX, offsetY);
    }

    /**
     * 获得图片缩放后的宽高，以及left, right, top, bottom
     *
     * @return 包含图片大小和位置的矩形
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mMatrix;
        RectF rectF = new RectF();

        Drawable drawable = getDrawable();
        if (drawable != null) {
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }

        return rectF;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    /**
     * 双击缩放图片时，使图片缩放产生动画效果的内部类
     */
    private class AutoScaleRunnable implements Runnable {

        /**
         * 每次缩小图片的固定比例
         */
        private static final float SMALLER = 0.9f;
        /**
         * 每次放大图片的固定比例
         */
        private static final float LARGER = 1.1f;

        /**
         * 最终放大的目标值
         */
        private float mTargetScale;
        /**
         * 每次缩放图片的比例值
         */
        private float mScaleValue = 1.0f;
        /**
         * 缩放图片X轴位置
         */
        private float mScaleX;
        /**
         * 缩放图片Y轴位置
         */
        private float mScaleY;

        AutoScaleRunnable(float targetScale, float scaleX, float scaleY) {
            mTargetScale = targetScale;
            mScaleX = scaleX;
            mScaleY = scaleY;
            mScaleValue = getScale() < mTargetScale ? LARGER : SMALLER;
        }

        @Override
        public void run() {
            mIsAutoScaling = true;

            mMatrix.postScale(mScaleValue, mScaleValue, mScaleX, mScaleY);
            drawableOffsetControl();
            setImageMatrix(mMatrix);

            // 缩放之后判断当前图片是否已达到最终需要缩放的大小
            float currentScale = getScale();
            if ((mScaleValue > 1.0f && mTargetScale > currentScale)// 正在放大，且未达到目标
                    || (mScaleValue < 1.0f && mTargetScale < currentScale)) {// 正在缩小且未达到目标
                postDelayed(this, 0);// 再次缩放
            } else {
                float finalScaleValue = mTargetScale / currentScale;
                mMatrix.postScale(finalScaleValue, finalScaleValue, mScaleX, mScaleY);
                drawableOffsetControl();
                setImageMatrix(mMatrix);

                mIsAutoScaling = false;
            }
        }
    }
}
