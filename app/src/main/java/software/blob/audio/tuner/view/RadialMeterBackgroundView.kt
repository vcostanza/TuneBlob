package software.blob.audio.tuner.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import kotlin.math.cos
import kotlin.math.sin

/**
 * The background for a tuning view that resembles an ammeter
 */
class RadialMeterBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : RadialMeterView(context, attrs, defStyleAttr) {

    // Used for drawing the arc
    private val startAngle: Float = minAngle.toFloat() + 270
    private val sweepAngle: Float = (maxAngle - minAngle).toFloat()

    /**
     * Draw the meter arc and tick lines for each major cent value
     * @param canvas View canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Reset drawing vars
        path.reset()
        paint.style = Paint.Style.FILL

        // Dimensions and positions
        val radiusArc = radius - density
        val radiusTick = radiusArc - tickLength
        val radiusText = radiusTick - (textSize + 1)
        val left = centerX - radiusArc
        val right = centerX + radiusArc
        val top = centerY - radiusArc
        val bottom = centerY + radiusArc

        // Add the top arc
        path.addArc(left, top, right, bottom, startAngle, sweepAngle)

        // Add the cent indicator ticks
        for (a in angles) {
            val rad = Math.toRadians(a.toDouble())
            val cos = cos(rad)
            val sin = sin(rad)
            val x1 = radiusX(sin, radiusArc)
            val y1 = radiusY(cos, radiusArc)
            val x2 = radiusX(sin, radiusTick)
            val y2 = radiusY(cos, radiusTick)
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)

            // Draw the text while we have offsets calculated
            val tx = radiusX(sin, radiusText)
            val ty = radiusY(cos, radiusText)
            val save = canvas.save()
            canvas.translate(tx, ty)
            canvas.rotate(a.toFloat())
            canvas.drawText(centString(a), 0f, 0f, paint)
            canvas.restoreToCount(save)
        }

        // Draw the meter background
        paint.style = Paint.Style.STROKE
        canvas.drawPath(path, paint)
    }

    /**
     * Show a plus sign next to cent values that are greater than zero
     * @param a Cent value (angle)
     */
    private fun centString(a: Int) = if (a > 0) "+$a" else a.toString()
}