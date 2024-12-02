package com.example.videoediting

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CustomRangeSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
        strokeWidth = 8f
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.FILL
    }

    private var thumbRadius = 20f
    var trackStart = 0f
    var trackEnd = 0f

    private var videoFrames: List<Bitmap>? = null
    private var scaledFrames: List<Bitmap>? = null

    var minValue: Float = 0f
    var maxValue: Float = 100f

    var minThumbValue = 0f
    var maxThumbValue = 100f

    private var listener: ((Float, Float) -> Unit)? = null

    init {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    handleTouch(event)
                    true
                }
                else -> false
            }
        }
    }

    fun setVideoFrames(frames: List<Bitmap>) {
        videoFrames = frames
        scaledFrames = null // Reset scaled frames for recalculation
        scaleFramesToFit()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        trackStart = paddingStart.toFloat()
        trackEnd = (width - paddingEnd).toFloat()
        scaleFramesToFit()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f

        // Draw video frames
        scaledFrames?.let {
            val frameWidth = (trackEnd - trackStart) / it.size
            for ((index, frame) in it.withIndex()) {
                val left = trackStart + index * frameWidth
                val top = centerY - (frame.height / 2)
                canvas.drawBitmap(frame, left, top, null)
            }
        }

        // Draw track
        //canvas.drawLine(trackStart, centerY, trackEnd, centerY, trackPaint)

        // Draw thumbs
//        val minThumbCenter = thumbXFromValue(minThumbValue)
//        val maxThumbCenter = thumbXFromValue(maxThumbValue)
//        canvas.drawCircle(minThumbCenter, centerY, thumbRadius, thumbPaint)
//        canvas.drawCircle(maxThumbCenter, centerY, thumbRadius, thumbPaint)

        val minThumbCenter = thumbXFromValue(minThumbValue)
        val maxThumbCenter = thumbXFromValue(maxThumbValue)

        drawSelectedRange(canvas, minThumbCenter, maxThumbCenter, centerY)

        drawRectangularThumb(canvas, minThumbCenter, centerY)
        drawRectangularThumb(canvas, maxThumbCenter, centerY)
    }

    private fun thumbXFromValue(value: Float): Float {
        return trackStart + (value / maxValue) * (trackEnd - trackStart)
    }

    private fun valueFromThumbX(x: Float): Float {
        return ((x - trackStart) / (trackEnd - trackStart)) * maxValue
    }

    private fun handleTouch(event: MotionEvent) {
        val x = event.x

        // Determine which thumb is closer
        val minThumbCenter = thumbXFromValue(minThumbValue)
        val maxThumbCenter = thumbXFromValue(maxThumbValue)

        val isMinThumbSelected = abs(minThumbCenter - x) < abs(maxThumbCenter - x)

        if (isMinThumbSelected) {
            val newValue = valueFromThumbX(x).coerceIn(0f, maxThumbValue)
            if (newValue != minThumbValue) {
                minThumbValue = newValue
                listener?.invoke(minThumbValue, maxThumbValue)
                invalidate()
            }
        } else {
            val newValue = valueFromThumbX(x).coerceIn(minThumbValue, maxValue)
            if (newValue != maxThumbValue) {
                maxThumbValue = newValue
                listener?.invoke(minThumbValue, maxThumbValue)
                invalidate()
            }
        }

        // Call the listener to notify the change
        listener?.invoke(minThumbValue, maxThumbValue)

        // Show the range in a Toast
        showRangeInToast(minThumbValue, maxThumbValue)
    }

    private fun scaleFramesToFit() {
        videoFrames?.let { frames ->
            if (frames.isNotEmpty()) {
                val frameWidth = ((trackEnd - trackStart) / frames.size).toInt()
                val frameHeight = (height * 0.6f).toInt()
                scaledFrames = frames.map {
                    Bitmap.createScaledBitmap(it, frameWidth, frameHeight, true)
                }
            }
        }
    }

    fun setOnRangeChangedListener(listener: (Float, Float) -> Unit) {
        this.listener = listener
    }


    private fun drawRectangularThumb(canvas: Canvas, x: Float, centerY: Float) {
        // Define dimensions for the rectangle
        val thumbWidth = 20f  // Width of the rectangle thumb
        val thumbHeight = 220f  // Height of the rectangle thumb

        // Calculate the left, right, top, and bottom positions for the thumb
        val left = x - (thumbWidth / 2)
        val right = x + (thumbWidth / 2)
        val top = centerY - (thumbHeight / 2)
        val bottom = centerY + (thumbHeight / 2)

        // Draw the rectangular thumb
        canvas.drawRoundRect(left, top, right, bottom, 10f, 10f, thumbPaint)  // Rounded rectangle
    }

    private fun drawSelectedRange(canvas: Canvas, minThumbCenter: Float, maxThumbCenter: Float, centerY: Float) {
        // Create paint with reduced opacity (alpha set to 100, which is less than 255 for full opacity)
        val selectedRangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, android.R.color.holo_blue_light)
            alpha = 100 // Adjust this value for the desired opacity (0-255)
        }

        // Calculate left and right positions for the selected range rectangle
        val left = minOf(minThumbCenter, maxThumbCenter)
        val right = maxOf(minThumbCenter, maxThumbCenter)

        // Draw the selected range as a rectangle
        val top = centerY - 79f
        val bottom = centerY + 79f

        canvas.drawRect(left, top, right, bottom, selectedRangePaint)
    }

    private fun showRangeInToast(minValue: Float, maxValue: Float) {
        val message = "Selected Range: $minValue - $maxValue"
       // Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
