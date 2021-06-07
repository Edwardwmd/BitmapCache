package com.edw.bitmapcachelibs.cache;

import android.graphics.Bitmap;

/*****************************************************************************************************
 * Project Name:    ImageLoaderLibCodeAnalysis
 *
 * Date:            2021-06-06
 *
 * Author:         EdwardWMD
 *
 * Github:          https://github.com/Edwardwmd
 *
 * Blog:            https://edwardwmd.github.io/
 *
 * Description:    所有Bitmap缓存数据的基类,之所以不适用HashMap是因为这样方便在
 ****************************************************************************************************
 */
public class BitmapCacheBean {
    //对应的键
    private String key;
    //对应的Bitmap
    private Bitmap mBitmap;

    public BitmapCacheBean(String key, Bitmap mBitmap) {
        super();
        this.key = key;
        this.mBitmap = mBitmap;

    }

    public BitmapCacheBean() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    @Override
    public String toString() {
        return "BitmapCacheBean{" +
                "key='" + key + '\'' +
                ", mBitmap=" + mBitmap +
                '}';
    }
}
