package com.github.stephanenicolas.kstock.network

data class CandlesResponse(
  val o: List<Float>,
  val h: List<Float>,
  val c: List<Float>,
  val l: List<Float>,
  val v: List<Float>,
  val t: List<Long>,
  val s: String,
)

