package com.edw.bitmapcachelibs.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;



import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/*****************************************************************************************************
 * Project Name:    ImageLoaderLibCodeAnalysis
 *
 * Date:            2021-06-02
 *
 * Author:         EdwardWMD
 *
 * Github:          https://github.com/Edwardwmd
 *
 * Blog:            https://edwardwmd.github.io/
 *
 * Description:    图片压缩，通过质量压缩、降低分辨率压缩、采样压缩
 ****************************************************************************************************
 */
public class BitmapDecodeCompress {
    private static final String TAG = "BitmapDecodeCompress";
    private static final float MAX_RATIO = 1;
    private static final float MIN_RATIO = 0.1f;

    /**
     * 通过质量quality（0~100）压缩图片
     *
     * @param outBitmap 待处理图片
     * @param quality   处理图片的质量占比
     * @param hasAlpha  是否需要透明度
     * @return 返回已处理的图片
     */
    public static Bitmap qualityCompress(Bitmap outBitmap, int quality, boolean hasAlpha) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream(2048);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            outBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] buff = baos.toByteArray();
            BitmapFactory.decodeByteArray(buff, 0, buff.length, options);
            if (!hasAlpha) {
                options.inPreferredConfig = Bitmap.Config.RGB_565;
            }
            options.inJustDecodeBounds = false;
            options.inMutable = true;
            options.inBitmap = outBitmap;
            return BitmapFactory.decodeByteArray(buff, 0, buff.length, options);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 通过减少单位尺寸的像素值，真正意义上的降低像素
     * 比率在0~1之间
     *
     * @param bitmap 位图
     */
    public static Bitmap pixelCompress(Bitmap bitmap, float ratio, boolean hasAlpha) {
        //保证图片压缩率在1~10的范围
        if (ratio > MAX_RATIO) {
            ratio = MAX_RATIO;
        }
        if (ratio < MIN_RATIO) {
            ratio = MIN_RATIO;
        }
        //获取压缩的图片，根据压缩倍率计算
        Bitmap newBitmap = Bitmap.createBitmap((int) (bitmap.getWidth() * ratio), (int) (bitmap.getHeight() * ratio), Bitmap.Config.ARGB_8888);
        //如果不需要透明度
        if (!hasAlpha) {
            newBitmap = Bitmap.createBitmap((int) (bitmap.getWidth() * ratio), (int) (bitmap.getHeight() * ratio), Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bitmap, null, new Rect(0, 0, (int) (bitmap.getWidth() * ratio), (int) (bitmap.getHeight() * ratio)), null);

        return newBitmap;
    }

    /**
     * 压缩图片
     *
     * @param mC          上下文
     * @param id          图片资源ID
     * @param maxHeight   图片最大限制高
     * @param maxWidth    图片最大限制宽
     * @param hasAlpha    是否需要透明度
     * @param reuseBitmap 复用图片
     * @return 位图
     */
    @SuppressLint("NewApi")
    public static Bitmap reSizeBitmap(Context mC, int id, int maxHeight, int maxWidth, boolean hasAlpha, Bitmap reuseBitmap) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;  //解码开关 处理图片相关信息类
        //只要没有赋值就表示不会创建内存空间
        BitmapFactory.decodeResource(mC.getResources(), id, options);
        //获取图片的宽
        int w = options.outWidth;
        //获取图片高
        int h = options.outHeight;
        //设置缩放系数
        options.inSampleSize = compressionFactor(w, h, maxWidth, maxHeight);
        //设置是否需要透明度
        if (!hasAlpha) {
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }

        options.inJustDecodeBounds = false;  //解码开关 处理完信息后，最后要切记关闭解码开关

        if (reuseBitmap != null) {
            if (w <= reuseBitmap.getWidth() && h <= reuseBitmap.getHeight()) {
                //表示图片在内存中可复用
                options.inMutable = true;
                //设置需要复用的Bitmap
                options.inBitmap = reuseBitmap;
            }
        }

        return BitmapFactory.decodeResource(mC.getResources(), id, options);
    }

    /**
     * 通过外面传回一张图片，通过将图片转换成流，使用采样率压缩图片
     *
     * @param bitmap    待处理图片
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @param hasAlpha  是否需要透明度
     * @return 返回一张通过采样率压缩的图片
     */
    public static Bitmap reSizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight, boolean hasAlpha) {
        ByteArrayOutputStream baos = null;
        try {

            baos = new ByteArrayOutputStream(2048);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] buff = baos.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(buff, 0, buff.length, options);
            int w = options.outWidth;
            int h = options.outHeight;
            options.inSampleSize = compressionFactor(w, h, maxWidth, maxHeight);
            if (!hasAlpha) {
                options.inPreferredConfig = Bitmap.Config.RGB_565;
            }
            options.inJustDecodeBounds = false;
            if (w <= bitmap.getWidth() || h <= bitmap.getHeight()) {
                options.inMutable = true;
                options.inBitmap = bitmap;
            }

            return BitmapFactory.decodeByteArray(buff, 0, buff.length, options);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return null;
    }


    /**
     * 图片压缩系数
     * 假设原图分辨率是1080*1080，被压缩的图片最大宽高是196*196，
     * 以2的倍率去压缩，一直压缩到与196*196最接近的分辨率，得出与之
     * 最接近的是135*135，缩放倍率是8，根据这个案例可得以下算法
     *
     * @param width     图片实际宽
     * @param height    图片实际高
     * @param maxWidth  图片最大宽度
     * @param maxHeight 图片最大高度
     * @return 压缩系数
     */
    private static int compressionFactor(int width, int height, int maxWidth, int maxHeight) {
        //默认系数是2
        int inSampleSize = 1;
        //递归除以2，直到width的大小小于或等于maxWidth且height的大小小于或等于maxHeight为止
        if (width > maxWidth && height > maxHeight) {
            inSampleSize = 2;
            while ((width / inSampleSize > maxWidth) && (height / inSampleSize > maxHeight)) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    /**
     * 将图片写入文件
     * 图片命名：时间戳.jpg
     *
     * @param bitmap 图片
     */
    public static void savePath(Context mC,Bitmap bitmap) {
        File file = new File(mC.getExternalFilesDir("").toString(), System.currentTimeMillis() + ".jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            bos.write(baos.toByteArray());
            baos.close();
            bos.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 将图片写入文件
     * 图片命名flag_时间戳.jpg
     *
     * @param bitmap 图片
     * @param flag   标记
     */
    public static void savePath(Context mC,Bitmap bitmap, String flag) {
        File file = new File(mC.getExternalFilesDir("").toString(), flag + "_" + System.currentTimeMillis() + ".jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            bos.write(baos.toByteArray());
            baos.close();
            bos.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
