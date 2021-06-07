package com.edw.bitmapcache.adapter

import com.edw.bitmapcache.entry.BitmapCatch
import com.edw.bitmapcache.R
import com.edw.bitmapcache.base.BaseAdapter
import com.edw.bitmapcache.base.BaseViewHolder
import com.edw.bitmapcache.databinding.ItemBitmapCacheBinding
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
 * Description:    本Demo旨在测试图片加载三级缓存的运用，三级缓存BitmapCacheLibs
 ****************************************************************************************************
 */
class BitmapCacheAdapter : BaseAdapter<BitmapCatch, ItemBitmapCacheBinding>() {
    override fun getLayoutRes(): Int = R.layout.item_bitmap_cache

    override fun onDataBindingViewHolder(
        binding: ItemBitmapCacheBinding?,
        curData: MutableList<BitmapCatch>,
        holder: BaseViewHolder,
        position: Int
    ) {
        binding?.apply {
            //在三级缓存中加载图片
            val bitmap = BitmapCache.getInstance().pullBitmap(curData, position, 280, 280)
            ivItem.setImageBitmap(bitmap!!)
        }
    }

}