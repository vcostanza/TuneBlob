package software.blob.audio.tuner.data

import software.blob.audio.util.Misc.indexToNoteName
import kotlin.math.round

/**
 * A sample in a [NoteCurve]
 */
class NoteSample(var time: Double, var note: Double) {

    init {
        set(time, note)
    }

    operator fun set(time: Double, note: Double) {
        this.time = time
        this.note = note
    }

    fun set(other: NoteSample) {
        set(other.time, other.note)
    }

    override fun toString(): String {
        val midRounded = round(this.note).toInt()
        val note = midRounded % 12
        val noteName = indexToNoteName(note)
        val octave = midRounded / 12 - 1
        val rem = round((note - midRounded.toDouble()) * 200).toInt()
        val remStr = (if (rem > 0) "+" else "") + rem + "%"
        return "$noteName$octave $remStr @ $time"
    }
}