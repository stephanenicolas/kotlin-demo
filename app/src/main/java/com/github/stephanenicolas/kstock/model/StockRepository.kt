package com.github.stephanenicolas.kstock.model

import java.util.ArrayList

object StockRepository {

    private val internalStocks: MutableList<Stock> = ArrayList()

    val stocks: List<Stock>
        get() = internalStocks.map { it }

    private val INITIAL_TICKERS = listOf(
        "TLT",
        "GLD",
        "SPY",
        "VPL",
    )

    init {
        INITIAL_TICKERS.mapTo(internalStocks) { Stock(it) }
    }

    fun updateStockItemPrice(symbol: String, price: Float) {
        val index = getStockIndex(symbol)
        internalStocks[index] = stocks[index].copy(price = price)
    }

    fun updateStockItemLastPrices(symbol: String, lastPrices: List<Float>) {
        val index = getStockIndex(symbol)
        internalStocks[index] = stocks[index].copy(lastPrices = lastPrices)
    }

    fun updateStockItemCandles(symbol: String, candles: List<Candle>) {
        val index = getStockIndex(symbol)
        internalStocks[index] = stocks[index].copy(candles = candles)
    }

    fun getStock(symbol: String?) = stocks.filter { it.symbol == symbol }.singleOrNull()

    /**
     * For diff util to work, we need to
     * pass a new list of items. Here we create a new
     * list with references to old items, except
     * for one that might have been updated.
     */
    fun copyStocks(symbol: String = "") =
        StockRepository
            .stocks
            .map {
                if (it.symbol == symbol)
                    it.copy()
                else
                    it
            }

    private fun getStockIndex(symbol: String) =
        stocks.indexOfFirst { stock -> stock.symbol == symbol }

}