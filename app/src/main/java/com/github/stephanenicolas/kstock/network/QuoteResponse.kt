package com.github.stephanenicolas.kstock.network

data class QuoteResponse(
  // Open price of the day
  val o: Float,
  // High price of the day
  val h: Float,
  // Low price of the day
  val l: Float,
  // Current price
  val c: Float,
  // Previous close price
  val pc: Float
)
