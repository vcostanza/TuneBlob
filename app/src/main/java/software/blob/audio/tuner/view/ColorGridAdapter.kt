package software.blob.audio.tuner.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.annotation.ArrayRes
import software.blob.audio.tuner.R

/**
 * Displays a grid of colored circles usually within a dialog
 */
class ColorGridAdapter(context: Context, @ArrayRes colorResId: Int = 0)
    : ArrayAdapter<Int>(context, R.layout.color_circle) {

    val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        // Add colors from a resource array
        if (colorResId != 0)
            addAll(context.resources.getIntArray(colorResId).toList())
    }

    /**
     * Create/update each color circle
     */
    override fun getView(position: Int, v: View?, parent: ViewGroup): View {
        val view = (v ?: inflater.inflate(R.layout.color_circle, parent, false)) as ImageView
        val color = getItem(position) ?: Color.WHITE
        view.setColorFilter(color)
        return view
    }
}