package com.github.stephanenicolas.kstock.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stephanenicolas.kstock.network.StockApi
import com.github.stephanenicolas.kstock.model.Candle
import com.github.stephanenicolas.kstock.model.StockRepository.copyStocks
import com.github.stephanenicolas.kstock.model.StockRepository.stocks
import com.github.stephanenicolas.kstock.model.StockRepository.updateStockItemCandles
import com.github.stephanenicolas.kstock.model.StockRepository.updateStockItemLastPrices
import com.github.stephanenicolas.kstock.model.StockRepository.updateStockItemPrice
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.ofEpochSecond
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class StockViewModel : ViewModel() {

    val data = MutableLiveData(copyStocks())
    private val stockApi = StockApi()

    fun loadQuotes() =
        viewModelScope.launch {
            stocks
                .map { it.symbol }
                .onEach { updateStockPrice(it) }
        }

    fun loadLastPrices() =
        viewModelScope.launch {
            stocks
                .map { it.symbol }
                .onEach { updateStockLastPrices(it) }
        }

    fun loadCandles(symbol: String) =
        viewModelScope.launch {
            updateStockCandles(symbol)
        }

    private suspend fun updateStockPrice(symbol: String) {
        val price = stockApi.quote(symbol).single().c
        updateStockItemPrice(symbol, price,)
        data.value = copyStocks(symbol)
    }

    private suspend fun updateStockLastPrices(symbol: String) {
        val today = LocalDateTime.now()
        val tenDaysAgo = LocalDate
            .now()
            .minus(BRIEF_HISTORY_LENGTH_IN_DAYS, ChronoUnit.DAYS)
            .atStartOfDay()
        val lastPrices = stockApi.lastPrices(symbol, tenDaysAgo, today).single()
        updateStockItemLastPrices(symbol, lastPrices)
        data.value = copyStocks(symbol)
    }

    private suspend fun updateStockCandles(symbol: String) {
        val today = LocalDateTime.now()
        val tenDaysAgo =
            LocalDate.now().minus(HISTORY_LENGTH_IN_DAYS, ChronoUnit.DAYS).atStartOfDay()
        val candlesResponse = stockApi.candles(symbol, tenDaysAgo, today).single()
        val candles = with(candlesResponse) {
            val s = t.map { it.toLocalDateTime() }
            o.mapIndexed { i, _ -> Candle(o[i], h[i], l[i], c[i], v[i], s[i]) }
        }
        updateStockItemCandles(symbol, candles)
        data.value = copyStocks(symbol)
    }

    private fun Long.toLocalDateTime() = ofEpochSecond(this, 0, ZoneOffset.UTC)

    companion object {
        const val BRIEF_HISTORY_LENGTH_IN_DAYS = 11L
        const val HISTORY_LENGTH_IN_DAYS = 150L
    }
}