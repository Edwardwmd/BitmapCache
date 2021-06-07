package com.edw.bitmapcache

import android.graphics.BitmapFactory
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.edw.bitmapcache.adapter.AtomItemDecoration
import com.edw.bitmapcache.adapter.BitmapCacheAdapter
import com.edw.bitmapcache.base.BaseActivity
import com.edw.bitmapcache.base.BaseAdapter.OnItemClickListener
import com.edw.bitmapcache.databinding.ActivityMainBinding
import com.edw.bitmapcache.entry.BitmapCatch

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var adapter: BitmapCacheAdapter? = null

    override fun getLayoutRes(): Int = R.layout.activity_main

    override fun initData() {
        adapter = BitmapCacheAdapter()
        binding?.apply {
            recy.layoutManager = LinearLayoutManager(this@MainActivity)
            recy.setHasFixedSize(true)
            recy.addItemDecoration(AtomItemDecoration())
            recy.adapter = adapter

            adapter!!.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(itemView: View, position: Int) {
                    Toast.makeText(this@MainActivity, "position: $position", Toast.LENGTH_SHORT)
                        .show()
                }

            })
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        adapter!!.setData(getData())
    }

    /**
     * 模拟网络加载进来的数据
     */
    fun getData(): MutableList<BitmapCatch> {
        return mutableListOf(
            BitmapCatch(
                "ca",
                BitmapFactory.decodeResource(resources, R.drawable.ic_car)
            ),
            BitmapCatch(
                "ko",
                BitmapFactory.decodeResource(resources, R.drawable.ic_ko)
            ),
            BitmapCatch(
                "po",
                BitmapFactory.decodeResource(resources, R.drawable.ic_po)
            ),
            BitmapCatch(
                "pso",
                BitmapFactory.decodeResource(resources, R.drawable.ic_pso)
            ),
            BitmapCatch(
                "so",
                BitmapFactory.decodeResource(resources, R.drawable.ic_so)
            ),
            BitmapCatch(
                "vi",
                BitmapFactory.decodeResource(resources, R.drawable.ic_view)
            )

        )
    }
}
