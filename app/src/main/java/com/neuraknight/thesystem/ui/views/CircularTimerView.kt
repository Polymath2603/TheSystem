package com.neuraknight.thesystem.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CircularTimerView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var timerColor: Int = Color.BLUE
        set(value) {
            field = value
            arcPaint.color = value
            invalidate()
        }

    var displayTime: String = "00:00"
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val size = (width.coerceAtMost(height) - paddingLeft - paddingRight).toFloat()
        val radius = size / 2f
        val x = (width - size) / 2f + paddingLeft
        val y = (height - size) / 2f + paddingTop

        val rectF = RectF(x + 20, y + 20, x + size - 20, y + size - 20)

        // Draw background circle
        canvas.drawArc(rectF, 0f, 360f, false, backgroundPaint)

        // Draw progress arc
        canvas.drawArc(rectF, -90f, progress * 360f, false, arcPaint)

        // Draw text
        canvas.drawText(
            displayTime,
            width / 2f,
            height / 2f + textPaint.textSize / 3f,
            textPaint
        )
    }
}
