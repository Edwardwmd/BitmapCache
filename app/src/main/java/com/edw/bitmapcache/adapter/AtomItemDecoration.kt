package com.edw.bitmapcache.adapter

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

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
class AtomItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = dp2Px(parent.context, 20)
        outRect.left = dp2Px(parent.context, 20)
        outRect.right = dp2Px(parent.context, 20)
    }

    private fun dp2Px(mC: Context, dp: Int): Int {
        val scale = mC.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}