package com.github.stephanenicolas.kstock.ui

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface StockItemDiffUtilAdapter {
    fun <T> notifyChanges(
        adapter: RecyclerView.Adapter<StockItemRecyclerViewAdapter.ViewHolder>,
        oldList: List<T>,
        newList: List<T>,
        compare: (oldItem: T, newItem: T) -> Boolean
    ) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                compare(oldList[oldItemPosition], newList[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition] == newList[newItemPosition]

            override fun getOldListSize() = oldList.size
            override fun getNewListSize() = newList.size
        })
        diff.dispatchUpdatesTo(adapter)
    }
}