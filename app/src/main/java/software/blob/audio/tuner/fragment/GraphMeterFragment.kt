package software.blob.audio.tuner.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import software.blob.audio.tuner.R
import software.blob.audio.tuner.databinding.GraphMeterFragmentBinding
import software.blob.audio.tuner.view.GraphMeterView

/**
 * Fragment that holds the [GraphMeterView]
 */
class GraphMeterFragment : TunerFragment() {

    private var _binding: GraphMeterFragmentBinding? = null
    private val binding get() = _binding!!

    /**
     * Inflate the view binding
     */
    override fun onCreateView(inf: LayoutInflater, root: ViewGroup?, state: Bundle?): View {
        super.onCreateView(inf, root, state)
        _binding = GraphMeterFragmentBinding.inflate(inf, root, false)
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
     * Add the latest note sample to the view
     * @param latestNote Latest note value
     * @param avgNote Average note value (unused here)
     * @param avgCents Cents value (unused here)
     */
    override fun addNoteSample(latestNote: Double, avgNote: Double, avgCents: Double) {
        binding.graphView.addSample(latestNote)
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
            binding.noteName.setTextColor(color)
            binding.noteCents.setTextColor(color)
        }
    }

    /**
     * Hide the text
     */
    override fun reset() = updateNoteText("", "")
}