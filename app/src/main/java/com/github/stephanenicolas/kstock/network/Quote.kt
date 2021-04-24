package com.github.stephanenicolas.kstock.network

data class Quote(
  // Open price of the day
  val o: String,
  // High price of the day
  val h: String,
  // Low price of the day
  val l: String,
  // Current price
  val c: String,
  // Previous close price
  val pc: String
)
