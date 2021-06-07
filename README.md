## 说明

在Android开发中，图片加载显得尤为重要，图片过大以及负优化会使得应用的性能大打折扣，试想下仅仅只在网络中加载图片着一种情况下，如果图片过大，首次加载就非常费时间。当用户退出此App，再次进入App时又要从网络加载图片,这样就会造成用户体验极差。这也是为什么图片加载在Android开放中显得这么重要的原因，所以加载图片的三级缓存来了，在很多大型的开源框架中都是用类似的方案执行图片加载的优化。
所谓图片的三级缓存就是：开始加载图片先从内存中加载图片，如果内存中没有图片，就从磁盘缓存中加载，如果磁盘缓存里没有图片就从网络中请求，将请求回来的图片展示并缓存到内存和磁盘缓存内。具体见下图(我这里只是做了模拟网络加载图片)：

<img src="/art/bitmap_cache_map.png" style="zoom:50%;" />

实现的核心代码：

```java
/**
 * 三级缓存的操作方法：
 * 首先先从内存缓存中获取数据，如果内存缓存没有数据，就在磁盘缓存中获取，如果磁盘缓存中也没有数据，
 * 就从网络中请请求数据，并将数据保存在内存缓存和磁盘缓存中。
 *
 * @param mDatas   数据（网络数据）
 * @param position （对应的图片位置）
 * @param <T>      数据类型
 * @return 最终拿到需要展示的图片
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
```



Demo展示的结果：

<img src="/art/bitmapcache.gif" style="zoom:50%;" />

