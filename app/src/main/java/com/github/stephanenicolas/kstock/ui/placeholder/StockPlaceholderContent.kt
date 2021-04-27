package com.github.stephanenicolas.kstock.ui.placeholder

import java.util.ArrayList
import java.util.HashMap

object StockPlaceholderContent {

  val STOCKS: MutableList<StockItem> = ArrayList()
  val MAP_STOCK: MutableMap<String, StockItem> = HashMap()

  val TICKERS = listOf(
    "TLT",
    "GLD",
    "SPY",
    "VPL",
  )

  init {
    TICKERS
      .onEach {
        addStockItem(createStockPlaceholderItem(it))
      }
  }

  fun updateStockItemPrice(symbol: String, price: Float) {
    val index = STOCKS.indexOfFirst { stock -> stock.symbol == symbol }
    STOCKS[index] = STOCKS[index].copy(price = price)
    MAP_STOCK[symbol] = STOCKS[index]
  }

  fun updateStockItemLastPrices(symbol: String, lastPrices: List<Float>) {
    val index = STOCKS.indexOfFirst { stock -> stock.symbol == symbol }
    STOCKS[index] = STOCKS[index].copy(lastPrices = lastPrices)
    MAP_STOCK[symbol] = STOCKS[index]
  }

  fun updateStockItemCandles(symbol: String, candles: List<Candle>) {
    val index = STOCKS.indexOfFirst { stock -> stock.symbol == symbol }
    STOCKS[index] = STOCKS[index].copy(candles = candles)
    MAP_STOCK[symbol] = STOCKS[index]
  }

  private fun addStockItem(itemStock: StockItem) {
    STOCKS.add(itemStock)
    MAP_STOCK[itemStock.symbol] = itemStock
  }

  private fun createStockPlaceholderItem(
    symbol: String,
    currentPrice: Float = 0f,
    lastPrices: List<Float> = emptyList(),
    candles: List<Candle> = emptyList()
  ): StockItem {
    return StockItem(symbol, currentPrice, lastPrices, candles)
  }

  /**
   * A placeholder item representing a piece of content.
   */
  data class StockItem(
    val symbol: String,
    val price: Float,
    val lastPrices: List<Float>,
    val candles: List<Candle>,
  ) {
    override fun toString(): String = symbol
  }
}