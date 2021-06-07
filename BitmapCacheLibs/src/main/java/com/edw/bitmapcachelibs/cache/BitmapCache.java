package com.edw.bitmapcachelibs.cache;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;


import com.edw.bitmapcachelibs.cache.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static android.os.Build.VERSION.SDK_INT;

/*****************************************************************************************************
 * Project Name:    ImageLoaderLibCodeAnalysis
 *
 * Date:            2021-06-05
 *
 * Author:         EdwardWMD
 *
 * Github:          https://github.com/Edwardwmd
 *
 * Blog:            https://edwardwmd.github.io/
 *
 * Description:  模仿Glide图片缓存，实现三级缓存，内存-》磁盘缓存-》网络
 ****************************************************************************************************
 */
public class BitmapCache {
    private static final String TAG = "BitmapCache";
    private Context mC;
    //内存缓存
    private LruCache<String, Bitmap> mCache;
    //磁盘缓存
    private DiskLruCache mDiskLruCache;
    //复用池
    private Set<WeakReference<Bitmap>> mReusePool;
    //引用队列(用来监听弱引用，这里是实时监听复用池，以保证复用池中哪些图片可用和不可用)
    private ReferenceQueue<Bitmap> mReferenceQueue;
    //处理引用队列的线程
    private Thread mClearThread;
    //关闭
    private boolean shutdown;


    BitmapFactory.Options options = new BitmapFactory.Options();


    @SuppressLint("StaticFieldLeak")
    private volatile static BitmapCache instance = null;

    public static BitmapCache getInstance() {
        if (instance == null) {
            synchronized (BitmapCache.class) {
                if (instance == null) {
                    instance = new BitmapCache();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化缓存以及磁盘缓存
     *
     * @param mC
     */
    public void init(Context mC) {
        this.mC = mC;
        ActivityManager am = (ActivityManager) mC.getSystemService(Context.ACTIVITY_SERVICE);
        //获取手机的可用内存
        int memoryClass = am.getMemoryClass();
        //复用池使用一个加锁可GC的弱引用HashSet数据结构
        mReusePool = Collections.synchronizedSet(new HashSet<WeakReference<Bitmap>>());
        //取1/8可用内存作为图片内存缓存
        mCache = new LruCache<String, Bitmap>(memoryClass / 8 * 1024 * 1024) {
            /**
             * 返回一张位图占用的内存大小
             * @param key 键
             * @param value 值
             * @return 单张图片占用的空间
             */
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //兼容Android4.4以后的版本
                if (SDK_INT > Build.VERSION_CODES.KITKAT) {
                    return value.getAllocationByteCount();
                }
                //兼容Android4.4以前的版本
                return value.getByteCount();
            }

            /**
             * 表示图片在缓存中占用的内存已经达到了极限，最先进入LruCache队列的那张图片就会被挤出来
             * @param evicted
             * @param key
             * @param oldValue 从队列中挤出来的图片（也是最早添加到队列里的那张图片）
             * @param newValue 从队列头新添加的那张图片
             */
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                //被挤出来的图片先判断是否可利用的（就是被复用过的）,如果是则将图片添加到复用池，如果否，则回收此图片。
                if (oldValue.isMutable()) {
                    mReusePool.add(new WeakReference<>(oldValue, getReferenceQueue()));
                } else {
                    oldValue.recycle();
                }
            }
        };
        try {
            //磁盘缓存初始化，默认给定最大15M大小的磁盘缓存，valueCount表示在磁盘中一张图片保存为一个文件
            mDiskLruCache = DiskLruCache.open(new File(mC.getExternalFilesDir("").toString()), 1, 1, 15 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 用于主动监听GC的API,加快引用回收，做这个操作主要是对某些版本垃圾回收机制在java中执行慢进行优化
     * 直接调用native层的recycle()对图片实现立马垃圾回收。
     *
     * @return 引用队列
     */
    private synchronized ReferenceQueue<Bitmap> getReferenceQueue() {
        if (null == mReferenceQueue) {
            mReferenceQueue = new ReferenceQueue<>();
            mClearThread = new Thread(() -> {
                while (!shutdown) {
                    try {
                        Reference<? extends Bitmap> reference = mReferenceQueue.remove();
                        Bitmap bitmap = reference.get();
                        if (null != bitmap && !bitmap.isRecycled()) {
                            //这是直接使用Native层回收，回收速度快
                            bitmap.recycle();
                        }

                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
            mClearThread.start();
        }
        return mReferenceQueue;
    }

    /**
     * 保存图片
     *
     * @param key    键
     * @param bitmap 要保存的图片
     */
    public void putBitmapToCache(String key, Bitmap bitmap) {
        mCache.put(key, bitmap);
    }

    /**
     * 获取图片
     *
     * @param key 键
     * @return 从缓存中拿到的图片
     */
    public Bitmap getBitmapFromCache(String key) {
        return mCache.get(key);
    }

    /**
     * 清除所有的缓存
     */
    public void clearAllCache() {
        mCache.evictAll();
    }

    @SuppressLint("ObsoleteSdkInt")
    public Bitmap getBitmapFromReusePool(int w, int h, int inSampleSize) {
        if (SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return null;
        }
        Bitmap reuseBitmap = null;
        //通过迭代器获取复用池的Bitmap
        Iterator<WeakReference<Bitmap>> iterator = mReusePool.iterator();
        while (iterator.hasNext()) {
            Bitmap bitmap = iterator.next().get();
            if (null != bitmap) {
                //可复用
                if (checkInBitmap(bitmap, w, h, inSampleSize)) {
                    reuseBitmap = bitmap;
                    iterator.remove();
                } else {
                    iterator.remove();
                }
            }

        }
        return reuseBitmap;

    }

    /**
     * 检查是否可复用
     *
     * @param bitmap       在复用池中的图片
     * @param w
     * @param h
     * @param inSampleSize 采样率
     * @return 是否可复用
     */
    private boolean checkInBitmap(Bitmap bitmap, int w, int h, int inSampleSize) {

        if (SDK_INT < Build.VERSION_CODES.KITKAT) {
            return w == bitmap.getWidth() && h == bitmap.getHeight() && inSampleSize == 1;
        }

        if (inSampleSize > 1) {
            w /= inSampleSize;
            h /= inSampleSize;
        }
        //一张图片大小=分辨率（w*h）*单个像素点所占的字符量。
        int byteCount = w * h * perPixelForByte(bitmap.getConfig());

        return byteCount <= bitmap.getAllocationByteCount();
    }

    /**
     * 图片单个像素点所占用的内存空间
     *
     * @param config 位图存储的类型，不同类型所占用内存也不同
     * @return 单个像素点所占用的内存空间
     */
    private int perPixelForByte(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        }
        return 2;
    }

    /**
     * 将图片缓存到磁盘
     *
     * @param key
     * @param bitmap
     */
    public void putBitmapToDisk(String key, Bitmap bitmap) {
        OutputStream os = null;
        try (DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key)) {
            //如果磁盘快照中没有这个图片
            if (null == snapshot) {
                //生成文件
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (null != editor) {
                    //通过降低质量压缩图片（0~100），100是未经过压缩的
                    os = editor.newOutputStream(0);
                    //按照50%质量压缩
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                    editor.commit();
                }
            }
            if (snapshot != null) {
                snapshot.close();
            }
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 从磁盘中获取图片
     *
     * @param key
     * @param reuseBitmap
     * @return
     */
    public Bitmap getBitmapFromDisk(String key, Bitmap reuseBitmap) {
        DiskLruCache.Snapshot snapshot = null;
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            snapshot = mDiskLruCache.get(key);
            if (snapshot == null) {
                return null;
            }
            is = snapshot.getInputStream(0);
            options.inMutable = true;
            options.inBitmap = reuseBitmap;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            //存到内存缓存里
            if (null != bitmap) {
                mCache.put(key, bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return bitmap;
    }

    /**
     * 三级缓存的操作方法：
     * 首先先从内存缓存中获取数据，如果内存缓存没有数据，就在磁盘缓存中获取，如果磁盘缓存中也没有数据，
     * 就从网络中请请求数据，并将数据保存在内存缓存和磁盘缓存中。
     *
     * @param mDatas   数据（网络数据）
     * @param position （对应的图片位置）
     * @param <T>      泛型
     * @return 要展示的图片
     */
    public <T extends BitmapCacheBean> Bitmap pullBitmap(List<T> mDatas, int position, int w, int h) {
        //先从内存缓存中拿数据
        Bitmap bitmap = getBitmapFromCache(mDatas.get(position).getKey());
        //如果内存缓存中没有数据
        if (bitmap == null) {
            //先从复用池中获取可复用的图片（如果不可复用复用池的图片就会被回收）
            Bitmap reuseBitmap = getBitmapFromReusePool(w, h, 2);
            //将复用池中可复用的图片取出放入磁盘缓存中
            bitmap = getBitmapFromDisk(mDatas.get(position).getKey(), reuseBitmap);
            //如果磁盘中没有图片，就需要从网络获取
            if (bitmap == null) {
                //图片先压缩处理
                bitmap = BitmapDecodeCompress.reSizeBitmap(mDatas.get(position).getmBitmap(), w, h, false);
                //将数据保存至内存缓存里
                putBitmapToCache(mDatas.get(position).getKey(), mDatas.get(position).getmBitmap());
                //将数据保存至磁盘缓存里
                putBitmapToDisk(mDatas.get(position).getKey(), mDatas.get(position).getmBitmap());
                Log.e(TAG, "图片从网络中里获取~~~");
                return bitmap;
            } else {
                Log.e(TAG, "图片从磁盘缓存里获取~~~");
                return bitmap;
            }
        } else {
            Log.e(TAG, "图片从内存缓存里获取~~~");
            return bitmap;
        }
    }


}
