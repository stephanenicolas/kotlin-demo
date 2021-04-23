package com.github.stephanenicolas.kstock.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/*
toto:c2082it37jksadq8le20
sandbox_c2082it37jksadq8le2g
 */
interface StockApiService {
  @GET("/api/v1/quote?token=c2082it37jksadq8le20")
  suspend fun getPrice(@Query("symbol") symbol: String): Quote
}
