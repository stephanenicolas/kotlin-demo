package com.github.stephanenicolas.kstock.network

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory

class StockApi {

  private val retrofit: Retrofit = Builder()
    .baseUrl("https://finnhub.io/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

  private val service: StockApiService = retrofit.create(StockApiService::class.java)

  fun quote(symbol: String): Flow<Quote> = flow {
    emit(service.getPrice(symbol))
  }.flowOn(IO)

}