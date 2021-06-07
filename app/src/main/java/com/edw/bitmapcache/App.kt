package com.edw.bitmapcache

import android.app.Application
import android.graphics.Bitmap
import com.edw.bitmapcachelibs.cache.BitmapCache

/*****************************************************************************************************
 * Project Name:    BitmapCache
 *
 * Date:            2021-06-07
 *
 * Author:         EdwardWMD
 *
 * Github:          https://github.com/Edwardwmd
 *
 * Blog:            https://edwardwmd.github.io/
 *
 * Description:    ToDo
 ****************************************************************************************************
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        //初始化图片三级缓存
        BitmapCache.getInstance().init(this)
    }
}