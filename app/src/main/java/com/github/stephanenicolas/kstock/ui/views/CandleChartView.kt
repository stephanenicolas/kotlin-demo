package com.github.stephanenicolas.kstock.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.github.stephanenicolas.kstock.ui.placeholder.Candle

class CandleChartView(context: Context, attrs: AttributeSet?): View(context, attrs) {

  private val gainPaint = Paint(0).apply {
    style = Paint.Style.STROKE
    color = Color.GREEN
    strokeWidth = 4f
  }

  private val lossPaint = Paint(0).apply {
    style = Paint.Style.STROKE
    color = Color.RED
    strokeWidth = 4f
  }

  private val backgroundPaint = Paint(0).apply {
    style = Paint.Style.FILL
    color = Color.WHITE
  }

  private val axesPaint = Paint(0).apply {
    style = Paint.Style.STROKE
    color = Color.BLUE
    strokeWidth = 4f
  }

  private val axisLabelPaint = TextPaint(0).apply {
    style = Paint.Style.FILL_AND_STROKE
    color = Color.BLUE
    textSize = 30f
    isAntiAlias = true
  }

  private val candleChartDrawPort = CandleChartDrawPort()

  fun setPrices(candles: List<Candle>) {
    candleChartDrawPort.setCandles(candles)
    invalidate()
    requestLayout()
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)

    var xpad = (paddingLeft + paddingRight).toFloat()
    val ypad = (paddingTop + paddingBottom).toFloat()

    candleChartDrawPort.viewWidth = w.toFloat() - xpad
    candleChartDrawPort.viewHeight = h.toFloat() - ypad
    candleChartDrawPort.viewPaddingLeft = paddingLeft.toFloat()
    candleChartDrawPort.viewPaddingTop = paddingTop.toFloat()
    candleChartDrawPort.viewPaddingRight = paddingRight.toFloat()
    candleChartDrawPort.viewPaddingBottom = paddingBottom.toFloat()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    candleChartDrawPort.clear(canvas, backgroundPaint)
    if (candleChartDrawPort.hasNoPrices()) {
      return
    }
    candleChartDrawPort.drawAxes(canvas, axesPaint, axisLabelPaint)
    candleChartDrawPort.drawCandles(canvas, gainPaint, lossPaint)
  }
}