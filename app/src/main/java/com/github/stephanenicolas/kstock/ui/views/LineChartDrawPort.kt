package com.github.stephanenicolas.kstock.ui.views

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF

class LineChartDrawPort {
    private val lastPrices: MutableList<Float> = mutableListOf()

    var viewWidth: Float = 0f
    var viewHeight: Float = 0f
    var viewPaddingLeft: Float = 0f
    var viewPaddingTop: Float = 0f
    var viewPaddingRight: Float = 0f
    var viewPaddingBottom: Float = 0f

    private var pricesRange: Float = 0f
    private var pricesMin: Float = 0f

    fun setLastPrices(prices: List<Float>) {
        lastPrices.clear()
        lastPrices.addAll(prices)

        if (lastPrices.isNotEmpty()) {
            pricesMin = lastPrices.minOf { it }
            pricesRange = lastPrices.maxOf { it } - pricesMin
        }
    }

    fun hasNoPrices() = lastPrices.isEmpty()

    fun clear(canvas: Canvas, backgroundPaint: Paint) {
        canvas.drawRect(
            0f,
            0f,
            viewPaddingLeft + viewWidth + viewPaddingRight,
            viewPaddingBottom + viewHeight + viewPaddingTop,
            backgroundPaint
        )
    }

    private fun Canvas.drawLine(pStart: PointF, pEnd: PointF, chartPaint: Paint) =
        drawLine(pStart.x, pStart.y, pEnd.x, pEnd.y, chartPaint)

    fun drawPrices(canvas: Canvas, chartPaint: Paint) {
        val xIncrement = viewWidth / lastPrices.size
        val viewMidHeight: Float = viewHeight / 2
        val heightPriceRatio = viewHeight / (2 * pricesRange)
        var lastPoint = createPricePoint(0, xIncrement, viewMidHeight, heightPriceRatio)

        canvas.apply {
            lastPrices
                .forEachIndexed { index, _ ->
                    if (index < lastPrices.size - 1) {
                        val pStart = lastPoint
                        val pEnd =
                            createPricePoint(index + 1, xIncrement, viewMidHeight, heightPriceRatio)
                        drawLine(pStart, pEnd, chartPaint)
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
        val x = viewPaddingLeft + index * xIncrement
        val y = viewPaddingTop + viewMidHeight - (price - pricesMin) * heightPriceRatio
        return PointF(x, y)
    }

    private fun createPricePoint(
        index: Int,
        xIncrement: Float,
        viewMidHeight: Float,
        heightPriceRatio: Float
    ): PointF {
        val price = lastPrices[index]
        return createPoint(index + 1, price, xIncrement, viewMidHeight, heightPriceRatio)
    }
}