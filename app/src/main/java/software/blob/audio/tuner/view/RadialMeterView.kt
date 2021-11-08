package software.blob.audio.tuner.view

import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import software.blob.audio.tuner.R
import kotlin.math.*

/**
 * Base class for the group of views that make up the radial meter tuning view
 */
abstract class RadialMeterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val path = Path()
    protected val density = resources.displayMetrics.density
    protected val textSize = resources.getDimension(R.dimen.radial_meter_text_size)
    protected val tickLength = resources.getDimension(R.dimen.radial_meter_tick_length)
    protected val angles: IntArray
    protected val minAngle: Int
    protected val maxAngle: Int

    // Drawing variables

    private var _diameter = 0f
    private var _dWidth = 0

    val diameter: Float get() {
        if (_dWidth != width) {
            val r = width.toFloat() / 2
            val minX = radiusX(sin(Math.toRadians(minAngle.toDouble())), r)
            val maxX = radiusX(sin(Math.toRadians(maxAngle.toDouble())), r)
            val angSpan = abs(maxX - minX)
            if (angSpan > 0)
                _diameter = width.toFloat().pow(2) / angSpan
            _dWidth = width
        }
        return _diameter
    }
    val radius: Float get() = diameter / 2
    val centerX: Float get() = width / 2f
    val centerY: Float get() = radius

    init {
        // Default paint parameters
        paint.color = context.getColor(R.color.radial_meter_outline)
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = density * 2

        // The angular extents of the meter
        var minAng = Integer.MAX_VALUE
        var maxAng = -Integer.MAX_VALUE
        angles = resources.getIntArray(R.array.radial_meter_angles)
        for (a in angles) {
            minAng = min(minAng, a)
            maxAng = max(maxAng, a)
        }
        minAngle = minAng
        maxAngle = maxAng
    }

    /**
     * Get the X-position for a given angle and radius
     * @param sin Pre-calculated sin(angle)
     * @param radius Radius in pixels
     * @return X-position
     */
    fun radiusX(sin: Double, radius: Float) = centerX + (sin * radius).toFloat()

    /**
     * Get the Y-position for a given angle and radius
     * @param cos Pre-calculated cos(angle)
     * @param radius Radius in pixels
     * @return Y-position
     */
    fun radiusY(cos: Double, radius: Float) = centerY - (cos * radius).toFloat()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val mHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        if (mWidth < mHeight)
            setMeasuredDimension(mWidth, mWidth)
        else
            setMeasuredDimension(mWidth, mHeight)
    }
}