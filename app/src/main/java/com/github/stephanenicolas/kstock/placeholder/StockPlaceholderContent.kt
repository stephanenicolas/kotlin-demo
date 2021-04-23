package com.github.stephanenicolas.kstock.placeholder

import java.util.ArrayList
import java.util.HashMap

object StockPlaceholderContent {

  val STOCKS: MutableList<StockPlaceholderItem> = ArrayList()
  val MAP_STOCK: MutableMap<String, StockPlaceholderItem> = HashMap()

  val tickers = listOf(
    "TLT",
    "GLD",
    "SPY",
    "VPL",
  )

  init {
    tickers
      .onEach {
        addItem(createStockPlaceholderItem(it))
      }
  }

  fun updateItem(symbol: String, price: String) {
    STOCKS[STOCKS.indexOfFirst { stock -> stock.symbol == symbol }] =
      createStockPlaceholderItem(symbol, price)
  }

  private fun addItem(itemStock: StockPlaceholderItem) {
    STOCKS.add(itemStock)
    MAP_STOCK[itemStock.symbol] = itemStock
  }

  private fun createStockPlaceholderItem(
    ticker: String,
    currentPrice: String = ""
  ): StockPlaceholderItem {
    return StockPlaceholderItem(ticker, currentPrice.toString(), "")
  }

  /**
   * A placeholder item representing a piece of content.
   */
  data class StockPlaceholderItem(
    val symbol: String,
    val price: String,
    val details: String
  ) {
    override fun toString(): String = symbol
  }
}