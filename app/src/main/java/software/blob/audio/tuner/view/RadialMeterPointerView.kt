package software.blob.audio.tuner.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import software.blob.audio.tuner.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * The moving pointer that goes on top of the [RadialMeterBackgroundView]
 */
class RadialMeterPointerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : RadialMeterView(context, attrs, defStyleAttr) {

    // The radius of the anchor that the pointer is attached to
    private val anchorSize = resources.getDimension(R.dimen.radial_meter_anchor_radius)

    // The displayed cents (angle)
    var cents = 0.0
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }

    // The color of the pointer
    var color: Int get() { return paint.color }
        set(value) {
            if (paint.color != value) {
                paint.color = value
                postInvalidate()
            }
        }

    init {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = context.getColor(R.color.title_text)
    }

    /**
     * Draw the pointer that indicates the cent value
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rad = Math.toRadians(cents)
        val cos = cos(rad)
        val sin = sin(rad)
        val radius1 = anchorSize
        val radius2 = radius - density - tickLength - textSize - anchorSize

        val x1 = radiusX(sin, radius1)
        val y1 = radiusY(cos, radius1)
        val x2 = radiusX(sin, radius2)
        val y2 = radiusY(cos, radius2)

        canvas.drawLine(x1, y1, x2, y2, paint)
        canvas.drawCircle(centerX, centerY, radius1, paint)
    }
}