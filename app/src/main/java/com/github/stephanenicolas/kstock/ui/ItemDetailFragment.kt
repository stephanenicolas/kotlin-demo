package com.github.stephanenicolas.kstock.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.github.stephanenicolas.kstock.R
import com.github.stephanenicolas.kstock.model.StockRepository
import com.github.stephanenicolas.kstock.databinding.FragmentItemDetailBinding
import com.github.stephanenicolas.kstock.model.Stock
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
  private var selectedStock: Stock? = null
  private lateinit var detailStockSymbolTextView: TextView
  private lateinit var detailStockPriceTextView: TextView
  private lateinit var candleChartView: CandleChartView

  private val viewModel by viewModels<StockViewModel>({requireActivity()})

  private var _binding: FragmentItemDetailBinding? = null

  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    selectedStock = viewModel.selectedStock.value

    viewModel.selectedStock.observe(this) { stock ->
      selectedStock = stock
      stock.candles?.let {
        candleChartView.setPrices(stock.candles)
      }
    }
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
    val rootView = binding.root

    binding.toolbarLayout?.title = selectedStock?.symbol

    detailStockSymbolTextView = binding.detailStockSymbol
    detailStockPriceTextView = binding.detailStockPrice!!
    candleChartView = binding.candleView!!

    selectedStock?.let {
      viewModel.loadCandles(it.symbol)
    }

    return rootView
  }


  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    // Inflate the menu; this adds items to the action bar if it is present.
    activity?.menuInflater?.inflate(R.menu.menu_main, menu)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}