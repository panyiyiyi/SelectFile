package com.even.selectfile.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Class Discription :图片加载优化处理
 * Created User : Even
 * Created Time : 2016/11/30.
 */

public class ImageLoader {
    //图片优化算法类
    private LruCache<String, Bitmap> mLruCacher;
    //线程池
    private ExecutorService mThreadPool;

    //任务队列
    private LinkedList<Runnable> mTasks;//队列调度方式
    private Type mType = Type.LIFO;
    //轮询的线程
    private Thread mPoolThread;

    private Handler mPoolThreadHander;
    //运行在UI线程的handler，用来设置imageView的图片
    private Handler mHandler;
    //引入一个值为1的信号量，防止mPoolThreadHander未初始化完成
    private volatile Semaphore mSemaphore = new Semaphore(1);
    //引入值为1的信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
    private volatile Semaphore mPoolSemaphore;

    private static ImageLoader mInstance;

    enum Type {
        LIFO, FIFO
    }

    /**
     * 单例
     *
     * @return
     */
    public static ImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(1, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    private void init(int threadCount, Type type) {
        try {
            mSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHander = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());
                        try {
                            mPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //释放一个信号量
                mSemaphore.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        //获取应用程序最大可用的内存,使用lru算法
        long maxMemory = Runtime.getRuntime().maxMemory();
        int cacheSize = (int) (maxMemory / 8);
        mLruCacher = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mPoolSemaphore = new Semaphore(threadCount);
        mTasks = new LinkedList<>();
        mType = type == null ? Type.LIFO : type;
    }

    /**
     * 取出任务
     *
     * @return
     */
    private synchronized Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTasks.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTasks.removeLast();
        }
        return null;
    }

    /**
     * 加载图片
     *
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView) {
        imageView.setTag(path);
        //UI线程
        if (mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImageBeanHolder holder = (ImageBeanHolder) msg.obj;
                    ImageView imageView = holder.imageView;
                    Bitmap bitmap = holder.bitmap;
                    String path = holder.path;
                    if (imageView.getTag().toString().equals(path)) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            };
        }
        Bitmap bm = getBitmapFromLruCache(path);
        if (bm != null) {
            ImageBeanHolder holder = new ImageBeanHolder();
            holder.bitmap = bm;
            holder.imageView = imageView;
            holder.path = path;

            Message message = Message.obtain();//保证message单一实例
            message.obj = holder;
            mHandler.sendMessage(message);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    ImageSize imageSize = getImageViewWidth(imageView);
                    int reqWidth = imageSize.width;
                    int reqHeight = imageSize.height;

                    Bitmap bitmap = decodeSampledBitmapFromResource(path, reqWidth, reqHeight);
                    addBitmapToLruCache(path, bitmap);
                    ImageBeanHolder holder = new ImageBeanHolder();
                    holder.bitmap = getBitmapFromLruCache(path);
                    holder.imageView = imageView;
                    holder.path = path;
                    Message message = Message.obtain();
                    message.obj = holder;

                    mHandler.sendMessage(message);
                    mPoolSemaphore.release();


                }
            });
        }


    }

    private void addBitmapToLruCache(String key, Bitmap bitmap) {
        if (getBitmapFromLruCache(key) == null)
            if (bitmap != null)
                mLruCacher.put(key, bitmap);
    }


    /**
     * 获取压缩后的图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        //调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        //使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);
        return bitmap;
    }

    /**
     * 计算inSampleSize,用于压缩图片
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //源图片的宽高
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int inSampleSize = 1;
        if (outWidth > reqWidth && outHeight > reqHeight) {
            //计算实际宽高和目标宽高的比例
            int widthRatio = Math.round((float) outWidth / (float) reqWidth);
            int heightRatio = Math.round((float) outHeight / (float) reqHeight);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }
        return inSampleSize;
    }


    /**
     * 根据imageView宽高压缩图片的宽高
     *
     * @param imageView
     * @return
     */
    private ImageSize getImageViewWidth(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();

        int width = layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView.getWidth();
        if (width <= 0) {
            width = layoutParams.width;
        }
        if (width <= 0)
            width = getImageViewFieldValue(imageView, "mMaxWidth");
        if (width <= 0)
            width = displayMetrics.widthPixels;
        int height = layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView.getHeight();
        if (height <= 0)
            height = layoutParams.height;
        if (height <= 0)
            height = getImageViewFieldValue(imageView, "mMaxHeight");
        if (height <= 0)
            height = displayMetrics.heightPixels;
        imageSize.width = width;
        imageSize.height = height;
        return imageSize;
    }

    /**
     * 反射获得ImageView设置的最大宽度和高度
     *
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE)
                value = fieldValue;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    /**
     * 图片宽高
     */
    private class ImageSize {
        int width;
        int height;
    }


    /**
     * 添加一个任务
     *
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        try {
            if (mPoolThreadHander == null)
                mSemaphore.acquire();

            mTasks.add(runnable);
            mPoolThreadHander.sendEmptyMessage(0x110);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 从LruCache中获取一张图片，如果不存在则返回null
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCacher.get(key);
    }

    private class ImageBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }


}
