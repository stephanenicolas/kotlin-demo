package com.github.stephanenicolas.kstock.network

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

  fun search(symbol: String): Flow<SymbolLookupResponse> = flow {
    emit(service.search(symbol))
  }.flowOn(IO)

  fun quote(symbol: String): Flow<QuoteResponse> = flow {
    emit(service.getPrice(symbol))
  }.flowOn(IO)

  fun lastPrices(symbol: String, from: LocalDateTime, to: LocalDateTime): Flow<List<Float>> = flow {
    val value =
      service.getCandles(symbol, from.toUnixTimeStamp().toString(), to.toUnixTimeStamp().toString()).c
    emit(value)
  }.flowOn(IO)

  fun candles(symbol: String, from: LocalDateTime, to: LocalDateTime): Flow<CandlesResponse> = flow {
    val value =
      service.getCandles(symbol, from.toUnixTimeStamp().toString(), to.toUnixTimeStamp().toString())
    emit(value)
  }.flowOn(IO)

  fun LocalDateTime.toUnixTimeStamp() = toEpochSecond(ZoneOffset.UTC)

  companion object {
    const val TIMEOUT = 30L
    const val BASE_URL = "https://finnhub.io/"
    const val DEBUG = true
  }
}