package com.github.stephanenicolas.kstock.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class LineChartPricesView(context: Context, attrs: AttributeSet) : View(context, attrs) {

  private val chartPaint = Paint(0).apply {
    style = Paint.Style.STROKE
    color = Color.BLUE
    strokeWidth = 4f
  }

  private val backgroundPaint = Paint(0).apply {
    style = Paint.Style.FILL
    color = Color.WHITE
  }

  private val chartDrawPort = LineChartDrawPort()

  fun setLastPrices(prices: List<Float>) {
    chartDrawPort.setLastPrices(prices)
    invalidate()
    requestLayout()
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)

    var xpad = (paddingLeft + paddingRight).toFloat()
    val ypad = (paddingTop + paddingBottom).toFloat()

    chartDrawPort.viewWidth = w.toFloat() - xpad
    chartDrawPort.viewPaddingLeft = paddingLeft.toFloat()
    chartDrawPort.viewHeight = h.toFloat() - ypad
    chartDrawPort.viewPaddingTop = paddingTop.toFloat()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    chartDrawPort.clear(canvas, backgroundPaint)
    if (chartDrawPort.hasNoPrices()) {
      return
    }
    chartDrawPort.drawPrices(canvas, chartPaint)
  }
}