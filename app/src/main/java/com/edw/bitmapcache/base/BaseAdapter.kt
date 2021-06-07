package com.edw.bitmapcache.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KProperty

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
 * Description:    Adapter基类
 ****************************************************************************************************
 */
 abstract class BaseAdapter<T, VB : ViewDataBinding> : RecyclerView.Adapter<BaseViewHolder>() {

    private val curData: MutableList<T> by lazy { mutableListOf() }

    private var listener: OnItemClickListener? = null

    open fun setData(mData: List<T>) {
        if (curData.size > 0) {
            curData.clear()
        }
        curData.addAll(mData)
        notifyDataSetChanged()
    }

    open fun removeAll() {
        if (curData.size > 0) curData.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val mBinding = DataBindingUtil.inflate<VB>(
            LayoutInflater.from(parent.context),
            getLayoutRes(),
            parent,
            false
        )
        return BaseViewHolder(mBinding.root)
    }


    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = DataBindingUtil.getBinding<VB>(holder.itemView)
        onDataBindingViewHolder(binding, curData, holder, position)
        holder.itemView.setOnClickListener {
            if (listener != null) {
                listener?.apply {
                    onItemClick(it!!, position)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return if (curData.size > 0) curData.size else 0
    }

    @LayoutRes
    abstract fun getLayoutRes(): Int

    abstract fun onDataBindingViewHolder(
        binding: VB?,
        curData: MutableList<T>,
        holder: BaseViewHolder,
        position: Int
    )

    /**
     * 条目点击事件
     */
    interface OnItemClickListener {
        fun onItemClick(itemView: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

}
