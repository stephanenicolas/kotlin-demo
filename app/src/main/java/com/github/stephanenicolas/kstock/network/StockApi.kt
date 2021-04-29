package com.github.stephanenicolas.kstock.network

import com.github.stephanenicolas.kstock.network.NetworkState.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit.SECONDS

class StockApi {
    private val loggingInterceptor = HttpLoggingInterceptor().also {
        if (DEBUG) it.level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient
        .Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT, SECONDS)
        .readTimeout(TIMEOUT, SECONDS)
        .build()

    private val retrofit: Retrofit = Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val service: StockApiService = retrofit.create(StockApiService::class.java)

    fun search(symbol: String): Flow<NetworkState<SymbolLookupResponse>> = flow {
        emit(Progress())
        emit(handleResponse(service.search(symbol)))
    }.flowOn(IO)

    fun quote(symbol: String): Flow<NetworkState<QuoteResponse>> = flow {
        emit(Progress())
        emit(handleResponse(service.getPrice(symbol)))
    }.flowOn(IO)

    fun lastPrices(
        symbol: String,
        from: LocalDateTime,
        to: LocalDateTime
    ): Flow<NetworkState<List<Float>>> = flow {
        val value =
            service.getCandles(
                symbol,
                from.toUnixTimeStamp().toString(),
                to.toUnixTimeStamp().toString()
            )
        emit(handleResponse(value) { it.c })
    }.flowOn(IO)

    fun candles(
        symbol: String,
        from: LocalDateTime,
        to: LocalDateTime
    ): Flow<NetworkState<CandlesResponse>> = flow {
        val value =
            service.getCandles(
                symbol,
                from.toUnixTimeStamp().toString(),
                to.toUnixTimeStamp().toString()
            )
        emit(handleResponse(value))
    }.flowOn(IO)

    private fun <T> handleResponse(
        response: Response<T>
    ): NetworkState<T> =
        if (response.body() == null) {
            InvalidData()
        } else {
            when (response.isSuccessful) {
                true -> Success(response.body()!!)
                else -> NetworkError(response.code())
            }
        }

    private fun <T, U> handleResponse(
        response: Response<T>,
        transform: ((T) -> U)
    ): NetworkState<U> =
        if (response.body() == null) {
            InvalidData()
        } else {
            when (response.isSuccessful) {
                true -> Success(transform(response.body()!!))
                else -> NetworkError(response.code())
            }
        }

    private fun LocalDateTime.toUnixTimeStamp() = toEpochSecond(ZoneOffset.UTC)

    companion object {
        const val TIMEOUT = 30L
        const val BASE_URL = "https://finnhub.io/"
        const val DEBUG = true
    }
}