package software.blob.audio.tuner.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import software.blob.audio.tuner.R
import software.blob.audio.tuner.databinding.RadialMeterFragmentBinding
import software.blob.audio.tuner.view.GraphMeterView

/**
 * Fragment that holds the [GraphMeterView]
 */
class RadialMeterFragment : TunerFragment() {

    private var _binding: RadialMeterFragmentBinding? = null
    private val binding get() = _binding!!

    /**
     * Inflate the view binding
     */
    override fun onCreateView(inf: LayoutInflater, root: ViewGroup?, state: Bundle?): View {
        super.onCreateView(inf, root, state)
        _binding = RadialMeterFragmentBinding.inflate(inf, root, false)
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
     * Set the pointer facing the latest cent value
     * @param latestNote Note value (unused here)
     * @param avgNote Average note value (unused here)
     * @param avgCents Average cents value
     */
    override fun addNoteSample(latestNote: Double, avgNote: Double, avgCents: Double) {
        binding.meterPointer.cents = avgCents
    }

    /**
     * Update the note/cents text display
     * @param noteName Formatted note name in scientific notation
     * @param noteCents Formatted cents value
     * @param tuned True if the note is within 10 cents of perfect tuning
     */
    override fun updateNoteText(noteName: String, noteCents: String, tuned: Boolean) {
        binding.noteName.text = noteName
        binding.noteCents.text = noteCents

        // Set color based on if the note is in tune (within 10 cents)
        val color = context?.getColor(if (tuned) R.color.in_tune_text else R.color.out_tune_text)
        if (color != null) {
            binding.meterPointer.color = color
            binding.noteName.setTextColor(color)
            binding.noteCents.setTextColor(color)
        }
    }

    /**
     * Hide the text and reset the pointer
     */
    override fun reset() {
        updateNoteText("", "")
        binding.meterPointer.cents = 0.0
        binding.meterPointer.color = context?.getColor(R.color.title_text) ?: Color.WHITE
    }
}