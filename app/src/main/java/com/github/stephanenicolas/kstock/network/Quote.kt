package com.github.stephanenicolas.kstock.network

data class Quote(
  // Open price of the day
  val o: Double,
  // High price of the day
  val h: Double,
  // Low price of the day
  val l: Double,
  // Current price
  val c: String,
  // Previous close price
  val pcDouble: Double
)
