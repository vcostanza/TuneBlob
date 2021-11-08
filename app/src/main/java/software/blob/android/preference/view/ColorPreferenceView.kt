package software.blob.android.preference.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import androidx.preference.PreferenceViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import software.blob.audio.tuner.R
import software.blob.android.preference.util.SimplePreferences
import software.blob.audio.tuner.view.ColorGridAdapter

/**
 * A preference for selecting colors
 */
class ColorPreferenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : PreferenceView(context, attrs, defStyleAttr) {

    private val prefs = SimplePreferences(context)
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var circle: ImageView? = null

    init {
        widgetLayoutResource = R.layout.color_circle
    }

    /**
     * Adds a color circle to the preference view
     * @param holder View holder
     */
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        // Add/update color circle
        circle = holder.findViewById(R.id.color_circle) as ImageView
        if (circle == null) return

        circle?.setColorFilter(prefs[key, Color.WHITE])
    }

    /**
     * Shows a color dialog when pressed
     */
    override fun onClick() {
        val adapter = ColorGridAdapter(context, R.array.color_wheel)
        val grid = inflater.inflate(R.layout.color_grid_dialog, null) as GridView
        grid.adapter = adapter

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(grid)
            .show()

        grid.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            // Update the color preference and view
            dialog.dismiss()
            val color = adapter.getItem(position)
            if (color != null) {
                prefs[key] = color
                circle?.setColorFilter(color)
            }
        }
    }
}