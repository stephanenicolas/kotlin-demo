package com.github.stephanenicolas.kstock.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.stephanenicolas.kstock.model.Candle
import com.github.stephanenicolas.kstock.model.Stock
import com.github.stephanenicolas.kstock.model.StockRepository
import com.github.stephanenicolas.kstock.model.StockRepository.copyStocks
import com.github.stephanenicolas.kstock.model.StockRepository.stocks
import com.github.stephanenicolas.kstock.model.StockRepository.updateStockItemLastPrices
import com.github.stephanenicolas.kstock.model.StockRepository.updateStockItemPrice
import com.github.stephanenicolas.kstock.network.NetworkState
import com.github.stephanenicolas.kstock.network.StockApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.ofEpochSecond
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class StockViewModel : ViewModel() {

    val data = MutableLiveData(copyStocks())
    val searchResults = MutableLiveData<List<Stock>>()
    val selectedStock = MutableLiveData<Stock>()
    val error = MutableLiveData<Int>()
    private val stockApi = StockApi()

    fun search(symbol: String) =
        viewModelScope.launch {
            stockApi.search(symbol).collect { networkState ->
                handleState(networkState) {
                    searchResults.value =
                        it.result.map { Stock(it.symbol, it.description) }
                }
            }
        }

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
        stockApi.quote(symbol).collect { networkState ->
            handleState(networkState) {
                updateStockItemPrice(symbol, it.c)
                data.value = copyStocks(symbol)
            }
        }
    }

    private suspend fun updateStockLastPrices(symbol: String) {
        val today = LocalDateTime.now()
        val tenDaysAgo = LocalDate
            .now()
            .minus(BRIEF_HISTORY_LENGTH_IN_DAYS, ChronoUnit.DAYS)
            .atStartOfDay()
        stockApi.lastPrices(symbol, tenDaysAgo, today).collect { networkState ->
            handleState(networkState) {
                updateStockItemLastPrices(symbol, it)
                data.value = copyStocks(symbol)
            }
        }
    }

    private suspend fun updateStockCandles(symbol: String) {
        val today = LocalDateTime.now()
        val tenDaysAgo =
            LocalDate.now().minus(HISTORY_LENGTH_IN_DAYS, ChronoUnit.DAYS).atStartOfDay()
        stockApi.candles(symbol, tenDaysAgo, today).collect { networkState ->
            handleState(networkState) {
                val candles = with(it) {
                    val s = t.map { it.toLocalDateTime() }
                    o.mapIndexed { i, _ -> Candle(o[i], h[i], l[i], c[i], v[i], s[i]) }
                }
                val stock = StockRepository.getStock(symbol)
                if (stock == null) {
                    selectedStock.value = Stock(symbol, candles = candles)
                } else {
                    selectedStock.value = stock.copy(candles = candles)
                }
            }
        }
    }

    private fun <T> handleState(
        networkState: NetworkState<T>,
        actionSuccess: (T) -> Unit
    ) =
        when (networkState) {
            is NetworkState.Success -> actionSuccess(networkState.data)
            is NetworkState.NetworkError -> error.value = networkState.code
            else -> Unit
        }

    private fun Long.toLocalDateTime() = ofEpochSecond(this, 0, ZoneOffset.UTC)

    companion object {
        const val BRIEF_HISTORY_LENGTH_IN_DAYS = 11L
        const val HISTORY_LENGTH_IN_DAYS = 150L
    }
}