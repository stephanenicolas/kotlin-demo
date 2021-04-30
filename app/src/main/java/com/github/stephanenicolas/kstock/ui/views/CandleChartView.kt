package com.github.stephanenicolas.kstock.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.github.stephanenicolas.kstock.R
import com.github.stephanenicolas.kstock.model.Candle

class CandleChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private lateinit var axesPaint: Paint
    private lateinit var axesLabelPaint: Paint
    private lateinit var chartPaint: Paint

    private val candleChartDrawPort = CandleChartDrawPort()

    init {
        initAttributes(context, attrs)
    }

    private fun initAttributes(context: Context, attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CandleChartPricesView,
            0, 0
        ).apply {

            try {
                chartPaint = Paint(0).apply {
                    style = Paint.Style.STROKE
                    color = getColor(
                        R.styleable.CandleChartPricesView_candleChartColor,
                        resources.getColor(R.color.white, null)
                    )
                    strokeWidth = 4f
                }
                axesPaint = Paint(0).apply {
                    style = Paint.Style.STROKE
                    color = getColor(
                        R.styleable.CandleChartPricesView_axesColor,
                        resources.getColor(R.color.white, null)
                    )
                    strokeWidth = 4f
                }

                axesLabelPaint = Paint(0).apply {
                    style = Paint.Style.FILL_AND_STROKE
                    textSize = 30f
                    isAntiAlias = true
                    color = getColor(
                        R.styleable.CandleChartPricesView_axesLabelColor,
                        resources.getColor(R.color.white, null)
                    )
                }
            } finally {
                recycle()
            }
        }
    }

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

        if (candleChartDrawPort.hasNoPrices()) {
            return
        }
        candleChartDrawPort.drawAxes(canvas, axesPaint, axesLabelPaint)
        candleChartDrawPort.drawCandles(canvas, chartPaint)
    }
}