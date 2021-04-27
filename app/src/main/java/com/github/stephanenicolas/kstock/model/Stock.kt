package com.github.stephanenicolas.kstock.model

data class Stock(
    val symbol: String,
    val price: Float? = 0f,
    val lastPrices: List<Float>? = emptyList(),
    val candles: List<Candle>? = emptyList(),
) {
    override fun toString(): String = symbol
}