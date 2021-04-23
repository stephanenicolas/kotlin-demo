package com.github.stephanenicolas.kstock.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stephanenicolas.kstock.network.StockApi
import com.github.stephanenicolas.kstock.placeholder.StockPlaceholderContent
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

class StockViewModel : ViewModel() {

  val data = MutableLiveData(copyStocks())
  private val stockApi = StockApi()

  fun loadQuotes() =
    viewModelScope.launch {
      StockPlaceholderContent
        .tickers
        .onEach { symbol ->
          updateSymbol(symbol)
        }
    }

  private suspend fun updateSymbol(symbol: String) {
    StockPlaceholderContent.updateItem(
      symbol,
      stockApi.quote(symbol).single().c
    )
    data.value = copyStocks(symbol)
  }

  /**
   * For diff util to work, we need to
   * pass a new list of items. Here we create a new
   * list with references to old items, except
   * for one that might have been updated.
   */
  private fun copyStocks(symbol: String= "") =
    StockPlaceholderContent
      .STOCKS
      .map {
        if(it.symbol == symbol)
          it.copy()
        else
          it
      }
}