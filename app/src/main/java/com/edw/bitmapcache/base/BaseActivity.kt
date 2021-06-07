package com.edw.bitmapcache.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

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
abstract class BaseActivity<VB : ViewDataBinding> : AppCompatActivity() {
    protected var binding: VB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutRes())

        initData()

    }

    @LayoutRes
    abstract fun getLayoutRes(): Int

    abstract fun initData()


    override fun onDestroy() {
        super.onDestroy()
        if (binding != null) {
            binding = null
        }
    }

}