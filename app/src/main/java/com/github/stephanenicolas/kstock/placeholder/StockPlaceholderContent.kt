package com.github.stephanenicolas.kstock.placeholder

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

  fun updateStockItemPrice(symbol: String, price: String) {
    val index = STOCKS.indexOfFirst { stock -> stock.symbol == symbol }
    STOCKS[index] = createStockPlaceholderItem(symbol, price, STOCKS[index].lastPrices)
  }

  fun updateStockItemLastPrices(symbol: String, lastPrices: List<Float>) {
    val index = STOCKS.indexOfFirst { stock -> stock.symbol == symbol }
    STOCKS[index] = createStockPlaceholderItem(symbol, STOCKS[index].price, lastPrices)
  }

  private fun addStockItem(itemStock: StockItem) {
    STOCKS.add(itemStock)
    MAP_STOCK[itemStock.symbol] = itemStock
  }

  private fun createStockPlaceholderItem(
    symbol: String,
    currentPrice: String = "",
    lastPrices: List<Float> = emptyList()
  ): StockItem {
    return StockItem(symbol, currentPrice, lastPrices)
  }

  /**
   * A placeholder item representing a piece of content.
   */
  data class StockItem(
    val symbol: String,
    val price: String,
    val lastPrices: List<Float>
  ) {
    override fun toString(): String = symbol
  }
}