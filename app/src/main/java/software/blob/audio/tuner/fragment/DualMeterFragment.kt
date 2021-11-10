package software.blob.audio.tuner.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import software.blob.audio.tuner.R
import software.blob.audio.tuner.databinding.DualMeterFragmentBinding
import software.blob.audio.tuner.view.GraphMeterView
import software.blob.audio.tuner.view.RadialMeterPointerView

/**
 * Fragment that holds both a [GraphMeterView] and [RadialMeterPointerView]
 */
class DualMeterFragment : TunerFragment() {

    private var _binding: DualMeterFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var radialMeter: RadialMeterPointerView

    /**
     * Inflate the view binding
     */
    override fun onCreateView(inf: LayoutInflater, root: ViewGroup?, state: Bundle?): View {
        super.onCreateView(inf, root, state)
        _binding = DualMeterFragmentBinding.inflate(inf, root, false)
        text = binding.text.root
        radialMeter = binding.radialView!!.meterPointer
        return binding.root
    }

    /**
     * Free up the binding instance
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Set the pointer facing the latest cent value and update the graph
     * @param latestNote Note value
     * @param avgNote Average note value
     * @param avgCents Average cents value
     */
    override fun addNoteSample(latestNote: Double, avgNote: Double, avgCents: Double) {
        radialMeter.cents = avgCents
        binding.graphView.addSample(latestNote)
    }

    /**
     * Update the radial meter display
     * @param noteName Formatted note name in scientific notation
     * @param noteCents Formatted cents value
     * @param tuned True if the note is within 10 cents of perfect tuning
     */
    override fun updateDisplay(noteName: String, noteCents: String, tuned: Boolean) {
        super.updateDisplay(noteName, noteCents, tuned)

        // Set color based on if the note is in tune (within 10 cents)
        val color = context?.getColor(if (tuned) R.color.in_tune_text else R.color.out_tune_text)
        if (color != null)
            radialMeter.color = color
    }

    /**
     * Hide the text and reset the pointer
     */
    override fun reset() {
        super.reset()
        radialMeter.cents = 0.0
        radialMeter.color = context?.getColor(R.color.title_text) ?: Color.WHITE
    }
}