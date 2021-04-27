package com.github.stephanenicolas.kstock.ui.views

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import com.github.stephanenicolas.kstock.model.Candle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CandleChartDrawPort {
  val candles: MutableList<Candle> = mutableListOf()

  var viewWidth: Float = 0f
  var viewHeight: Float = 0f
  var viewPaddingLeft: Float = 0f
  var viewPaddingTop: Float = 0f
  var viewPaddingRight: Float = 0f
  var viewPaddingBottom: Float = 0f
  var xAxisLabelwidth: Float = 0f
  var xAxisLabelHeight: Float = 0f
  var xAxisLabelMargin: Float = 0f
  var yAxisLabelwidth: Float = 0f
  var yAxisLabelHeight: Float = 0f
  var yAxisLabelMargin: Float = 0f

  private val xAxisFormatter = DateTimeFormatter.ofPattern("MMM.uuuu")
  private var pricesRange: Float = 0f
  private var pricesMin: Float = 0f

  fun setCandles(candles: List<Candle>) {
    this.candles.clear()
    this.candles.addAll(candles)

    if (candles.isNotEmpty()) {
      pricesMin = candles.minOf { it.lowPrice }
      pricesRange = candles.maxOf { it.highPrice } - pricesMin
    }
  }

  fun hasNoPrices() = candles.isEmpty()

  fun clear(canvas: Canvas, backgroundPaint: Paint) {
    canvas.drawRect(0f, 0f, viewWidth, viewHeight, backgroundPaint)
  }

  private fun Canvas.drawLine(pStart: PointF, pEnd: PointF, chartPaint: Paint) =
    drawLine(pStart.x, pStart.y, pEnd.x, pEnd.y, chartPaint)

  fun drawAxes(canvas: Canvas, axesPaint: Paint, axisLabelPaint: Paint) {
    var bounds = Rect()
    val mockPriceLabel = "0000"
    axisLabelPaint.getTextBounds(mockPriceLabel, 0, mockPriceLabel.length, bounds)
    yAxisLabelwidth = bounds.right.toFloat()
    yAxisLabelHeight = -bounds.top.toFloat()
    yAxisLabelMargin = yAxisLabelHeight / 5

    bounds = Rect()
    val mockDate = LocalDateTime.now()
    val mockDateLabel = xAxisFormatter.format(mockDate)
    axisLabelPaint.getTextBounds(mockDateLabel, 0, mockDateLabel.length, bounds)
    xAxisLabelwidth = bounds.right.toFloat()
    xAxisLabelHeight = -bounds.top.toFloat()
    xAxisLabelMargin = xAxisLabelHeight / 5
    drawXAxis(canvas, axesPaint, axisLabelPaint)
    drawYAxis(canvas, axesPaint, axisLabelPaint)
  }

  private fun drawXAxis(
    canvas: Canvas,
    axesPaint: Paint,
    axisLabelPaint: Paint
  ) {
    val xAxisLeft = xAxisLeft()
    val xAxisRight = xAxisRight()
    val xAxisHeight = xAxisHeight()
    canvas.drawLine(
      xAxisLeft,
      xAxisHeight,
      xAxisRight,
      xAxisHeight,
      axesPaint
    )

    if (candles.isNotEmpty()) {
      val xAxisIntervalCount = xAxisTickCount - 1
      val xAxisCandleTickInterval = candles.size / xAxisIntervalCount
      val xAxisLabelLeft = xAxisLeft - xAxisLabelwidth / 2 + xAxisLabelMargin
      val xAxisLabelRight = xAxisRight - xAxisLabelwidth - xAxisLabelMargin
      val xAxisLabelHeight = xAxisHeight + xAxisLabelHeight + xAxisLabelMargin
      val xAxisTickInterval = (xAxisLabelRight - xAxisLabelLeft) / xAxisIntervalCount

      for (xAxisLabelIndex in 0 until xAxisTickCount) {
        val indexCandle = (xAxisLabelIndex * xAxisCandleTickInterval).coerceAtMost(candles.size - 1)
        canvas.drawText(
          xAxisFormatter.format(candles[indexCandle].timeStamp),
          xAxisLabelLeft + xAxisLabelIndex * xAxisTickInterval,
          xAxisLabelHeight,
          axisLabelPaint
        )
      }
    }
  }

  private fun drawYAxis(
    canvas: Canvas,
    axesPaint: Paint,
    axisLabelPaint: Paint
  ) {
    val yAxisLeft = xAxisLeft()
    val yAxisTop = viewPaddingTop
    val yAxisBottom = xAxisHeight()
    canvas.drawLine(
      yAxisLeft,
      yAxisTop,
      yAxisLeft,
      yAxisBottom,
      axesPaint
    )

    if (candles.isNotEmpty()) {
      val yAxisLabelLeft = viewPaddingLeft + yAxisLabelMargin
      val yAxisLabelStart = yAxisBottom - yAxisLabelMargin
      @Suppress("UnnecessaryVariable")
      val yAxisLabelEnd = yAxisTop + yAxisLabelHeight + yAxisLabelMargin
      val yAxisIntervalCount = yAxisTickCount - 1
      val yAxisTickInterval = (yAxisLabelStart - yAxisLabelEnd) / yAxisIntervalCount

      val minPrice = candles.minOf { it.lowPrice }
      val maxPrice = candles.maxOf { it.highPrice }
      val yAxisPriceTickInterval = (maxPrice - minPrice) / yAxisIntervalCount

      for (yAxisLabelIndex in 0 until yAxisTickCount) {
        val priceLabel = (minPrice + yAxisLabelIndex * yAxisPriceTickInterval).toInt().toString()
        canvas.drawText(
          priceLabel,
          yAxisLabelLeft,
          yAxisLabelStart - yAxisLabelIndex * yAxisTickInterval,
          axisLabelPaint
        )
      }
    }
  }

  fun drawCandles(canvas: Canvas, gainPaint: Paint, lossPaint: Paint) {
    val xAxisLeft = xAxisLeft()
    val xAxisHeight = xAxisHeight()
    val xIncrement = (viewWidth - yAxisLabelwidth) / candles.size
    val heightPriceRatio = (xAxisHeight - viewPaddingTop) / pricesRange

    var currentPoint = createPricePoint(0, xIncrement, xAxisLeft, xAxisHeight, heightPriceRatio)
    candles
      .forEachIndexed { index, _ ->
        if (index < candles.size - 1) {
          val pStart = currentPoint
          val pEnd =
            createPricePoint(index + 1, xIncrement, xAxisLeft, xAxisHeight, heightPriceRatio)
          canvas.drawLine(pStart, pEnd, gainPaint)
          currentPoint = pEnd
        }
      }
  }

  private fun xAxisHeight() =
    viewPaddingTop + viewHeight - xAxisLabelHeight - 2 * xAxisLabelMargin

  private fun xAxisRight() = viewPaddingLeft + viewWidth

  private fun xAxisLeft() = viewPaddingLeft + yAxisLabelMargin + yAxisLabelwidth + yAxisLabelMargin

  private fun createPoint(
    index: Int,
    price: Float,
    xIncrement: Float,
    xAxisLeft: Float,
    xAxisHeight: Float,
    heightPriceRatio: Float
  ): PointF {
    val x = xAxisLeft + index * xIncrement
    val y = xAxisHeight - (price - pricesMin) * heightPriceRatio
    return PointF(x, y)
  }

  private fun createPricePoint(
    index: Int,
    xIncrement: Float,
    xAxisLeft: Float,
    xAxisHeight: Float,
    heightPriceRatio: Float
  ): PointF {
    val price = candles[index]
    return createPoint(index, price.closePrice, xIncrement, xAxisLeft, xAxisHeight, heightPriceRatio)
  }

  companion object {
    const val xAxisTickCount = 5
    const val yAxisTickCount = 6
  }
}