package software.blob.audio.tuner.data

/**
 * A collection of [NoteSample] objects
 */
class NoteCurve : ArrayList<NoteSample>() {
    /**
     * Get the last sample in the curve
     */
    fun last() : NoteSample? {
        return if (isNotEmpty()) this[size - 1] else null
    }
}