package com.github.stephanenicolas.kstock.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stephanenicolas.kstock.network.StockApi
import com.github.stephanenicolas.kstock.placeholder.StockPlaceholderContent
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class StockViewModel : ViewModel() {

  val data = MutableLiveData(copyStocks())
  private val stockApi = StockApi()

  fun loadQuotes() =
    viewModelScope.launch {
      StockPlaceholderContent
        .TICKERS
        .onEach { symbol ->
          updateStockPrice(symbol)
        }
    }

  fun loadLastPrices() =
    viewModelScope.launch {
      StockPlaceholderContent
        .TICKERS
        .onEach { symbol ->
          updateStockLastPrices(symbol)
        }
    }

  private suspend fun updateStockPrice(symbol: String) {
    StockPlaceholderContent.updateStockItemPrice(
      symbol,
      stockApi.quote(symbol).single().c,
    )
    data.value = copyStocks(symbol)
  }

  private suspend fun updateStockLastPrices(symbol: String) {
    val today = LocalDateTime.now()
    val tenDaysAgo = LocalDate.now().minus(HISTORY_LENGTH_IN_DAYS, ChronoUnit.DAYS).atStartOfDay()
    StockPlaceholderContent.updateStockItemLastPrices(
      symbol,
      stockApi.candles(symbol, tenDaysAgo, today).single().c
    )
    data.value = copyStocks(symbol)
  }

  /**
   * For diff util to work, we need to
   * pass a new list of items. Here we create a new
   * list with references to old items, except
   * for one that might have been updated.
   */
  private fun copyStocks(symbol: String = "") =
    StockPlaceholderContent
      .STOCKS
      .map {
        if (it.symbol == symbol)
          it.copy()
        else
          it
      }

  companion object {
    const val HISTORY_LENGTH_IN_DAYS = 11L
  }
}