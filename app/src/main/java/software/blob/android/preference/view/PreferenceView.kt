package software.blob.android.preference.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.preference.SeekBarPreference
import software.blob.audio.tuner.R

/**
 * A [Preference] with more easily expandable layout capabilities
 */
open class PreferenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : Preference(context, attrs, defStyleAttr) {

    var bottomWidgetLayoutResource: Int = 0

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        layoutResource = R.layout.preference_view

        val a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceView, 0, 0)
        bottomWidgetLayoutResource = a.getResourceId(
            R.styleable.PreferenceView_bottomWidgetLayout, 0)
        a.recycle()
    }

    /**
     * Handles the bottom widget layout res ID
     * @param holder Preference view holder
     */
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val sb: SeekBarPreference

        // Add the bottom widget if specified
        if (bottomWidgetLayoutResource != 0) {
            // Add the custom layout if it isn't already added
            val bottom = holder.bottomWidgetFrame
            if (bottom.childCount == 0) {
                val layout = inflater.inflate(bottomWidgetLayoutResource, bottom, false)
                bottom.addView(layout)
                bottom.visibility = View.VISIBLE
            }
        }
    }

    /* Preference view holder component shortcuts */

    val PreferenceViewHolder.title: TextView get() =
        findViewById(android.R.id.title) as TextView

    val PreferenceViewHolder.summary: TextView get() =
        findViewById(android.R.id.summary) as TextView

    val PreferenceViewHolder.icon: ImageView get() =
        findViewById(android.R.id.icon) as ImageView

    val PreferenceViewHolder.iconFrame: ViewGroup get() =
        findViewById(android.R.id.icon_frame) as ViewGroup

    val PreferenceViewHolder.widgetFrame: ViewGroup get() =
        findViewById(android.R.id.widget_frame) as ViewGroup

    val PreferenceViewHolder.bottomWidgetFrame: ViewGroup get() =
        findViewById(R.id.bottom_widget_frame) as ViewGroup
}