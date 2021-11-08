package software.blob.android.preference.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import software.blob.audio.tuner.R
import java.text.DecimalFormat
import kotlin.math.roundToInt

/**
 * A number preference controlled by a seekbar
 * This has some extra functionality compared to the AndroidX equivalent
 */
open class SeekBarPreferenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : PreferenceView(context, attrs, defStyleAttr),
    SeekBar.OnSeekBarChangeListener {

    protected enum class Type {
        INT, FLOAT
    }

    protected var type = Type.INT
    protected var min = 0f
    protected var max = 0f
    protected var interval = 0f
    protected var intRange = 0
    protected var showValue = true
    protected var unit: String? = null

    init {
        bottomWidgetLayoutResource = R.layout.preference_seekbar

        val a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreferenceView, 0, 0)

        // Read attributes
        val typeInt = a.getInt(R.styleable.SeekBarPreferenceView_type, 0)
        type = if (typeInt == 0) Type.INT else Type.FLOAT
        min = a.getFloat(R.styleable.SeekBarPreferenceView_minValue, 0f)
        max = a.getFloat(R.styleable.SeekBarPreferenceView_maxValue, 0f)
        interval = a.getFloat(R.styleable.SeekBarPreferenceView_interval, 0f)
        showValue = a.getBoolean(R.styleable.SeekBarPreferenceView_showValue, true)
        unit = a.getString(R.styleable.SeekBarPreferenceView_unit)

        // Calculate integer range of the bar
        intRange = ((max - min) / interval).roundToInt()

        a.recycle()
    }

    /**
     * Update the seek bar and text
     * @param holder View holder
     */
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val value = if (type == Type.INT) getPersistedInt(0) else getPersistedFloat(0f)

        val valueTxt = holder.findViewById(R.id.seekbar_value) as TextView
        valueTxt.text = formatValue(value)
        valueTxt.visibility = if (showValue) View.VISIBLE else View.GONE

        val seekbar = holder.findViewById(R.id.seekbar) as SeekBar
        seekbar.setOnSeekBarChangeListener(null)
        seekbar.min = 0
        seekbar.max = intRange
        seekbar.progress = valueToProgress(value)
        seekbar.tag = valueTxt
        seekbar.setOnSeekBarChangeListener(this)
    }

    /**
     * Nothing to do when movement begins
     */
    override fun onStartTrackingTouch(seekBar: SeekBar) {
    }

    /**
     * Update the display value
     */
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val valueTxt = seekBar.tag as TextView?
        if (valueTxt != null) valueTxt.text = formatValue(progressToValue(seekBar.progress))
    }

    /**
     * When the seek bar is not longer being moved, persist the value
     */
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val value = progressToValue(seekBar.progress)
        if (type == Type.INT)
            persistInt(value.toInt())
        else
            persistFloat(value)
    }

    /**
     * Formats values to a string for display
     * This method can be implemented by sub-classes to do custom formatting
     * @param value Numeric value to format
     * @return Value as string
     */
    protected open fun formatValue(value: Number): String {
        var str = if (type == Type.INT) value.toInt().toString() else decFormat.format(value)
        if (unit != null) str += unit
        return str
    }

    /**
     * Convert preference value to the seek bar progress value
     * @param value Preference value
     * @return Seek bar progress
     */
    protected fun valueToProgress(value: Number) = ((value.toFloat() - min) / interval).toInt()

    /**
     * Convert seek bar progress value to preference value
     * @param progress Seek bar progress
     * @return Preference value
     */
    protected fun progressToValue(progress: Int) = (progress * interval) + min

    companion object {
        // Formats floats to one decimal place w/out extra zero
        private val decFormat = DecimalFormat("#.##")
    }
}