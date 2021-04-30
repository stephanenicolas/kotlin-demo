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

    val gainColor = android.R.color.holo_green_light
    val lossColor = android.R.color.holo_red_light

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
        with(holder) {
            val item = items[position]
            symbolView.text = item.symbol

            val currentPrice = item.price
            priceView.text = currentPrice?.roundToString()

            val lastClosePrice = item.lastPrices?.getOrNull(item.lastPrices?.size - 2)
            val dayGain = currentPrice?.minus(lastClosePrice ?: 0f)
            dayGainView.text = dayGain?.roundSignedToString()
            val dayGainPercent = dayGain?.times(100f)?.div(lastClosePrice ?: 1f)
            dayGainPercentView.text = dayGainPercent?.roundSignedPercentToString()
            dayGain?.let {
                val colorResId = if (dayGain >= 0) gainColor else lossColor
                val color = dayGainView.resources.getColor(colorResId, null)
                dayGainView.setTextColor(color)
                dayGainPercentView.setTextColor(color)
            }
            lineChartPricesChartView.setLastPrices(item.lastPrices ?: emptyList())

            itemView.tag = item
            itemView.setOnClickListener(onClickListener)
        }
    }

    override fun getItemCount() = items.size

    private fun Float.roundToString(decimals: Int = 2): String = "%.${decimals}f".format(this)
    private fun Float.roundSignedToString(decimals: Int = 2): String = "%+.${decimals}f".format(this)
    private fun Float.roundSignedPercentToString(decimals: Int = 2): String = roundSignedToString()?.plus("%")

    inner class ViewHolder(binding: ItemStockBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        val symbolView: TextView = binding.stockItemSymbol
        val priceView: TextView = binding.stockItemPrice
        val dayGainPercentView: TextView = binding.stockItemDayGainPercent
        val dayGainView: TextView = binding.stockItemDayGain
        val lineChartPricesChartView: LineChartPricesView = binding.lastPricesChart
    }
}