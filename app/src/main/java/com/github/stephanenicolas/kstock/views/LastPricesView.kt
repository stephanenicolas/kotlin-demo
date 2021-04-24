package com.github.stephanenicolas.kstock.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View

class LastPricesView(context: Context, attrs: AttributeSet) : View(context, attrs) {

  private val chartPaint = Paint(0).apply {
    style = Paint.Style.STROKE
    color = Color.BLUE
    strokeWidth = 4f
  }

  private val backgroundPaint = Paint(0).apply {
    style = Paint.Style.FILL
    color = Color.WHITE
  }

  private val lastPrices: MutableList<Float> = mutableListOf()
  private var viewWidth: Float = 0f
  private var viewHeight: Float = 0f

  private var pricesRange: Float = 0f
  private var pricesMin: Float = 0f

  fun setLastPrices(prices: List<Float>) {
    lastPrices.clear()
    lastPrices.addAll(prices)

    if (lastPrices.isNotEmpty()) {
      pricesMin = lastPrices.minOf { it }
      pricesRange = lastPrices.maxOf { it } - pricesMin
    }
    invalidate()
    requestLayout()
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)

    // Account for padding
    var xpad = (paddingLeft + paddingRight).toFloat()
    val ypad = (paddingTop + paddingBottom).toFloat()

    viewWidth = w.toFloat() - xpad
    viewHeight = h.toFloat() - ypad
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    canvas.clear()
    if (lastPrices.isEmpty()) {
      return
    }

    val xIncrement = viewWidth / lastPrices.size
    val viewMidHeight: Float = viewHeight / 2
    val heightPriceRatio = viewHeight / (2 * pricesRange)
    var lastPoint = createPricePoint(0, xIncrement, viewMidHeight, heightPriceRatio)

    canvas.apply {
      lastPrices
        .forEachIndexed { index, _ ->
          if (index < lastPrices.size - 1) {
            val pStart = lastPoint
            val pEnd = createPricePoint(index + 1, xIncrement, viewMidHeight, heightPriceRatio)
            drawLine(pStart, pEnd)
            lastPoint = pEnd
          }
        }
    }
  }

  private fun createPoint(
    index: Int,
    price: Float,
    xIncrement: Float,
    viewMidHeight: Float,
    heightPriceRatio: Float
  ): PointF {
    val x = paddingLeft.toFloat() + index * xIncrement
    val y = paddingTop.toFloat() + viewMidHeight - (price - pricesMin) * heightPriceRatio
    return PointF(x, y)
  }

  private fun createPricePoint(
    index: Int,
    xIncrement: Float,
    viewMidHeight: Float,
    heightPriceRatio: Float
  ) : PointF {
    val price = lastPrices[index]
    return createPoint(index + 1, price, xIncrement, viewMidHeight, heightPriceRatio)
  }

  private fun Canvas.clear() = drawRect(0f, 0f, viewWidth, viewHeight, backgroundPaint)

  private fun Canvas.drawLine(pStart: PointF, pEnd: PointF) =
    drawLine(pStart.x, pStart.y, pEnd.x, pEnd.y, chartPaint)
}