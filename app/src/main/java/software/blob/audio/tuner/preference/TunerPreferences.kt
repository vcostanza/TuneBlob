package software.blob.audio.tuner.preference

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatDelegate
import software.blob.audio.tuner.R
import software.blob.android.preference.util.SimplePreferences
import software.blob.audio.tuner.view.GraphMeterView

/**
 * Preferences for TuneBlob
 */
class TunerPreferences(context: Context) : SimplePreferences(context) {

    /**
     * Set default preference values
     */
    init {
        // Theme
        setDefault(R.string.pref_night_mode, AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)

        // Current fragment (radial, graph, or dual)
        setDefault(R.string.pref_active_fragment, "dual")

        // Engine parameters
        setDefault(R.string.pref_min_input_volume, -40f)
        setDefault(R.string.pref_max_input_frequency, 1000f)
        setDefault(R.string.pref_tuning_standard, 440f)

        // Graph meter view
        setDefault(R.string.pref_graph_line_color, Color.RED)
        setDefault(R.string.pref_graph_time_range, 3f)
        setDefault(R.string.pref_graph_note_range, 10f)
        setDefault(R.string.pref_graph_time_thresh, 0.2f)
        setDefault(R.string.pref_graph_note_thresh, 1f)
        setDefault(R.string.pref_graph_focus_lag, 0.85f)
    }

    /**
     * Day/night mode preference
     */
    var nightMode: Int get() { return getInt(R.string.pref_night_mode) }
        set(value) { this[R.string.pref_night_mode] = value }

    /**
     * Active fragment (radial meter view or graph meter view)
     */
    var activeFragment: String get() { return getString(R.string.pref_active_fragment) }
        set(value) { this[R.string.pref_active_fragment] = value }

    /**
     * The minimum input volume (decibels)
     */
    var minInputVolume: Float get() { return getFloat(R.string.pref_min_input_volume) }
        set(value) { this[R.string.pref_min_input_volume] = value }

    /**
     * The maximum input frequency (hertz)
     */
    var maxInputFrequency: Float get() { return getFloat(R.string.pref_max_input_frequency) }
        set(value) { this[R.string.pref_max_input_frequency] = value }

    /**
     * The A4 tuning standard (hertz)
     */
    var tuningStandard: Float get() { return getFloat(R.string.pref_tuning_standard) }
        set(value) { this[R.string.pref_tuning_standard] = value }

    /**
     * The displayed time range for the [GraphMeterView] (seconds)
     */
    var graphTimeRange: Float get() { return getFloat(R.string.pref_graph_time_range) }
        set(value) { this[R.string.pref_graph_time_range] = value }

    /**
     * The displayed note range for the [GraphMeterView]
     */
    var graphNoteRange: Float get() { return getFloat(R.string.pref_graph_note_range) }
        set(value) { this[R.string.pref_graph_note_range] = value }

    /**
     * The color of the note segments displayed in the [GraphMeterView]
     */
    var graphLineColor: Int get() { return getInt(R.string.pref_graph_line_color) }
        set(value) { this[R.string.pref_graph_line_color] = value }

    /**
     * The time threshold used to break up segments displayed in the [GraphMeterView] (seconds)
     */
    var graphTimeThresh: Float get() { return getFloat(R.string.pref_graph_time_thresh) }
        set(value) { this[R.string.pref_graph_time_thresh] = value }

    /**
     * The note threshold used to break up segments displayed in the [GraphMeterView]
     */
    var graphNoteThresh: Float get() { return getFloat(R.string.pref_graph_note_thresh) }
        set(value) { this[R.string.pref_graph_note_thresh] = value }

    /**
     * The amount of time it takes to focus on the latest note in the [GraphMeterView] (seconds)
     */
    var graphFocusLag: Float get() { return getFloat(R.string.pref_graph_focus_lag) }
        set(value) { this[R.string.pref_graph_focus_lag] = value }
}