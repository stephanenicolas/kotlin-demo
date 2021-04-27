package com.github.stephanenicolas.kstock.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.stephanenicolas.kstock.ui.placeholder.StockPlaceholderContent
import com.github.stephanenicolas.kstock.databinding.FragmentItemDetailBinding
import com.github.stephanenicolas.kstock.ui.views.CandleChartView
import com.github.stephanenicolas.kstock.viewmodel.StockViewModel

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListFragment]
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

  /**
   * The placeholder content this fragment is presenting.
   */
  private var itemStock: StockPlaceholderContent.StockItem? = null
  private lateinit var itemDetailTextView: TextView
  private lateinit var candleChartView: CandleChartView

  private val viewModel by viewModels<StockViewModel>()

  private var _binding: FragmentItemDetailBinding? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      if (it.containsKey(ARG_ITEM_ID)) {
        // Load the placeholder content specified by the fragment
        // arguments. In a real-world scenario, use a Loader
        // to load content from a content provider.
        itemStock = StockPlaceholderContent.MAP_STOCK[it.getString(ARG_ITEM_ID)]
      }
    }

    viewModel.data.observe(this) { stockList ->
      val stockItem = stockList
        .filter { it.symbol == itemStock!!.symbol }
        .single()

      candleChartView.setPrices(stockItem.candles)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
    val rootView = binding.root

    binding.toolbarLayout?.title = itemStock?.symbol

    itemDetailTextView = binding.itemDetail
    candleChartView = binding.candleView!!

    itemStock?.let {
      viewModel.loadCandles(it.symbol)
    }

    return rootView
  }

  companion object {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    const val ARG_ITEM_ID = "item_id"
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}