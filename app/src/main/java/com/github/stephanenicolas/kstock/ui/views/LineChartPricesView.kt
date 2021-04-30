package com.github.stephanenicolas.kstock.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.View
import com.github.stephanenicolas.kstock.R

class LineChartPricesView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private lateinit var chartPaint : Paint

    private val chartDrawPort = LineChartDrawPort()

    fun setLastPrices(prices: List<Float>) {
        chartDrawPort.setLastPrices(prices)
        invalidate()
        requestLayout()
    }

    init {
        initAttributes(context, attrs)
    }

    private fun initAttributes(context: Context, attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LineChartPricesView,
            0, 0
        ).apply {

            try {


                chartPaint = Paint(0).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                    color = getColor(
                        R.styleable.LineChartPricesView_lineChartColor,
                        resources.getColor(R.color.white, null)
                    )
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        var xpad = (paddingLeft + paddingRight).toFloat()
        val ypad = (paddingTop + paddingBottom).toFloat()

        chartDrawPort.viewWidth = w.toFloat() - xpad
        chartDrawPort.viewHeight = h.toFloat() - ypad
        chartDrawPort.viewPaddingLeft = paddingLeft.toFloat()
        chartDrawPort.viewPaddingTop = paddingTop.toFloat()
        chartDrawPort.viewPaddingRight = paddingRight.toFloat()
        chartDrawPort.viewPaddingBottom = paddingBottom.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //chartDrawPort.clear(canvas, backgroundPaint)
        if (chartDrawPort.hasNoPrices()) {
            return
        }
        chartDrawPort.drawPrices(canvas, chartPaint)
    }
}