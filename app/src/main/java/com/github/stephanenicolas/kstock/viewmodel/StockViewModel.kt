package com.github.stephanenicolas.kstock.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stephanenicolas.kstock.network.StockApi
import com.github.stephanenicolas.kstock.ui.placeholder.Candle
import com.github.stephanenicolas.kstock.ui.placeholder.StockPlaceholderContent
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
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

  fun loadCandles(symbol: String) =
    viewModelScope.launch {
      updateStockCandles(symbol)
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
    val tenDaysAgo = LocalDate.now().minus(BRIEF_HISTORY_LENGTH_IN_DAYS, ChronoUnit.DAYS).atStartOfDay()
    StockPlaceholderContent.updateStockItemLastPrices(
      symbol,
      stockApi.lastPrices(symbol, tenDaysAgo, today).single()
    )
    data.value = copyStocks(symbol)
  }

  private suspend fun updateStockCandles(symbol: String) {
    val today = LocalDateTime.now()
    val tenDaysAgo = LocalDate.now().minus(HISTORY_LENGTH_IN_DAYS, ChronoUnit.DAYS).atStartOfDay()
    val candlesResponse = stockApi.candles(symbol, tenDaysAgo, today).single()
    val candles = with(candlesResponse) {
      zip(o, h, l, c, v, t) { open, high, low, current, volume, timestamp ->
        Candle(
          open,
          high,
          low,
          current,
          volume,
          LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)
        )
      }
    }
    StockPlaceholderContent.updateStockItemCandles(
      symbol,
      candles
    )
    data.value = copyStocks(symbol)
  }

  inline fun zip(
    opens: List<Float>,
    highs: List<Float>,
    lows: List<Float>,
    closes: List<Float>,
    volumes: List<Float>,
    timestamps: List<Long>,
    transform: (
      o: Float,
      h: Float,
      l: Float,
      c: Float,
      v: Float,
      t: Long,
    ) -> Candle
  ): List<Candle> {
    val result = mutableListOf<Candle>()
    var i = 0
    while (i < opens.size) {
      result.add(
        transform(
          opens[i],
          highs[i],
          lows[i],
          closes[i],
          volumes[i],
          timestamps[i],
        )
      )
      i++
    }

    return result
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
    const val BRIEF_HISTORY_LENGTH_IN_DAYS = 11L
    const val HISTORY_LENGTH_IN_DAYS = 150L
  }
}