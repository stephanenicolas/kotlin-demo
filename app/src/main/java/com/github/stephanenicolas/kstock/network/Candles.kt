package com.github.stephanenicolas.kstock.network

data class Candles(
  val o: List<Float>,
  val h: List<Float>,
  val c: List<Float>,
  val l: List<Float>,
  val v: List<Float>,
  val s: String,
)

