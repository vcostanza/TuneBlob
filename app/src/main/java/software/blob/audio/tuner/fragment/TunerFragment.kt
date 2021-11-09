package software.blob.audio.tuner.fragment

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import software.blob.android.collections.FIFOList
import software.blob.android.thread.BasicIntervalThread
import software.blob.audio.tuner.R
import software.blob.audio.tuner.engine.TunerInputEngine
import software.blob.audio.tuner.preference.TunerPreferences
import software.blob.audio.tuner.view.NoteTextLayout
import software.blob.audio.util.Misc
import kotlin.math.abs
import kotlin.math.roundToInt

private const val TAG = "TunerFragment"

/**
 * Base fragment class for views driven by the [TunerInputEngine]
 */
abstract class TunerFragment : Fragment() {

    // Preferences
    private lateinit var prefs: TunerPreferences

    // Components
    private lateinit var audioManager: AudioManager
    private lateinit var engine: TunerInputEngine
    protected var text: NoteTextLayout? = null

    // Window of latest frequency readings that are averaged together for smoother results
    private var readSize: Int = 20
    private val readings = FIFOList<Double>(readSize)

    /**
     * Initialize the components for this fragment
     * Sub-classes should call the super for this method while providing their inflated view
     * @param inf Layout inflater
     * @param root Root view group that this fragment is stored in
     * @param state Saved state instance
     */
    @CallSuper
    override fun onCreateView(inf: LayoutInflater, root: ViewGroup?, state: Bundle?): View? {

        val context = requireContext()

        // Initialize preferences
        prefs = TunerPreferences(context)

        // Get audio devices
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Create the audio input engine
        engine = TunerInputEngine()

        // Return value should not be used
        return null
    }

    /**
     * Start the input engine when the app has resumed
     */
    @CallSuper
    override fun onResume() {
        super.onResume()
        startEngine()
    }

    /**
     * Stop the input engine when the app has paused
     */
    @CallSuper
    override fun onPause() {
        super.onPause()
        stopEngine()
    }

    /**
     * Add a note sample from the tuner engine
     * This is called from the [BasicIntervalThread] that pulls samples from the engine
     * @param latestNote Latest note value
     * @param avgNote Average note based on the last few readings
     * @param avgCents Cents value (-50 to +50; based on [avgNote])
     */
    abstract fun addNoteSample(latestNote: Double, avgNote: Double, avgCents: Double)

    /**
     * Update the views which displays the current note (called on UI Thread)
     * @param noteName The note name (scientific notation)
     * @param noteCents Formatted note cents (-50 to 50)
     * @param tuned True if the note is within 10 cents of perfect tuning
     */
    @UiThread
    open fun updateDisplay(noteName: String, noteCents: String, tuned: Boolean = false) {
        text?.visibility = View.VISIBLE
        text?.setText(noteName, noteCents)

        // Set color based on if the note is in tune (within 10 cents)
        val color = context?.getColor(if (tuned) R.color.in_tune_text else R.color.out_tune_text)
        if (color != null)
            text?.color = color
    }

    /**
     * Called when input hasn't been received for a period of time
     */
    @UiThread
    open fun reset() {
        text?.visibility = View.INVISIBLE
    }

    /**
     * Start the input engine
     */
    private fun startEngine() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
        if (devices.isEmpty()) {
            Log.e(TAG, "No input devices found")
            return
        }

        val device = devices[0]
        val minAmp = Misc.getAmplitude(prefs.minInputVolume.toDouble()).toFloat()
        val tuningStandard = prefs.tuningStandard.toDouble()

        if (!engine.setParameters(0.2f, minAmp, prefs.maxInputFrequency)) {
            Log.e(TAG, "Failed to set parameters on engine")
            return
        }

        val success = engine.start(device.id, 1, 48000)
        if (!success) {
            Log.e(TAG, "Failed to start engine")
            return
        }

        Log.d(TAG, "Started tuner engine")

        // Query frequency 60 frames per second and send them to the tuner view
        var lastNonZero = System.currentTimeMillis()
        val thread = BasicIntervalThread(60) {

            // Pull a frequency sample from the engine
            val freq = engine.queryFrequency().toDouble()

            var avgFreq = 0.0
            val t = System.currentTimeMillis()

            // Check that the note is valid (input detected)
            if (freq > 0) {

                // Add to the FIFO
                readings.add(freq)

                // Track that we got a non-zero value
                lastNonZero = t

                // Get the average based on readings
                for (v in readings) avgFreq += v
                avgFreq /= readings.size

            } else {

                // The amount of time since we last received some input
                val silenceTime = t - lastNonZero

                // Clear readings if there's no input after 1 second
                if (silenceTime >= 1000) readings.clear()

                // Clear text if there's no input after 5 seconds
                if (silenceTime >= 5000)
                    runOnUiThread { reset() }

                return@BasicIntervalThread
            }

            // Convert to a note value
            val latestNote = Misc.getNoteValue(freq, tuningStandard)
            val avgNote = Misc.getNoteValue(avgFreq, tuningStandard)

            // Compute the cents value from average note
            val noteInt = avgNote.roundToInt()
            val cents = (avgNote - noteInt) * 100
            val tuned = abs(cents) <= 10

            // Add the note to the tuner view
            addNoteSample(latestNote, avgNote, cents)

            // Formatting for UI text
            val noteName = Misc.getNoteName(noteInt)
            val centsStr = (if (cents >= 1) "+" else "") + cents.toInt()

            // Update text
            runOnUiThread {
                updateDisplay(noteName, getString(R.string.cent_format, centsStr), tuned)
            }
        }

        // The thread should only run while the engine is active
        thread.setRunCondition { engine.active }

        // Start the reader thread
        thread.start()
    }

    /**
     * Stop the input engine
     */
    private fun stopEngine() {
        if (engine.stop())
            Log.d(TAG, "Stopped tuner engine")
        else
            Log.e(TAG, "Failed to stop tuner engine")
    }

    /**
     * Run method on UI thread without crashing if the context is null
     * @param method Method to execute on the UI thread
     */
    private fun runOnUiThread(method: () -> Unit) {
        activity?.runOnUiThread { if (context != null) method() }
    }
}