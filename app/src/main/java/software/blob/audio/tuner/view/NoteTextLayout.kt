package software.blob.audio.tuner.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import software.blob.audio.tuner.R

/**
 * A [TextView] that displays the name of a note and its microtonal variation in cents
 */
class NoteTextLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var note: TextView
    private lateinit var cents: TextView

    /**
     * The color of the text
     */
    var color: Int get() { return _color }
        set(value) {
            if (_color != value) {
                _color = value
                note.setTextColor(_color)
                cents.setTextColor(_color)
            }
        }
    private var _color = Color.WHITE

    /**
     * Set the text for both views
     * @param note Note name
     * @param cents Cents string
     */
    fun setText(note: String, cents: String) {
        this.note.text = note
        this.cents.text = cents
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        note = findViewById(R.id.note)
        cents = findViewById(R.id.cents)
    }
}