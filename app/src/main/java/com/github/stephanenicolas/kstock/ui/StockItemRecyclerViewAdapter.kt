package com.github.stephanenicolas.kstock.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.stephanenicolas.kstock.databinding.ItemStockBinding
import com.github.stephanenicolas.kstock.model.Stock
import com.github.stephanenicolas.kstock.ui.StockItemRecyclerViewAdapter.ViewHolder
import com.github.stephanenicolas.kstock.ui.views.LineChartPricesView
import kotlin.properties.Delegates

class StockItemRecyclerViewAdapter(
    values: List<Stock>,
    private val onClickListener: View.OnClickListener
) :
    RecyclerView.Adapter<ViewHolder>(), StockItemDiffUtilAdapter {

    var items: List<Stock> by Delegates.observable(emptyList()) { _, oldList, newList ->
        notifyChanges(this, oldList, newList) { oldItem, newItem ->
            oldItem.symbol == newItem.symbol
        }
    }

    init {
        items = values
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemStockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.symbolView.text = item.symbol
        holder.priceView.text = item.price?.round()
        item.lastPrices?.let {
            holder.lineChartPricesChartView.setLastPrices(item.lastPrices)
        }

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = items.size

    private fun Float.round(decimals: Int = 2): String = "%.${decimals}f".format(this)

    inner class ViewHolder(binding: ItemStockBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        val symbolView: TextView = binding.stockItemSymbol
        val priceView: TextView = binding.stockItemPrice
        val lineChartPricesChartView: LineChartPricesView = binding.lastPricesChart
    }
}