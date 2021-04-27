package com.github.stephanenicolas.kstock.ui.placeholder

import java.time.LocalDateTime

data class Candle(val openPrice: Float,
  val highPrice: Float,
  val lowPrice: Float,
  val closePrice: Float,
  val volume: Float,
  val timeStamp: LocalDateTime
)