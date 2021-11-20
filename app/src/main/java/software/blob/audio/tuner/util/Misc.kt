package software.blob.audio.tuner.util

import kotlin.math.*

/**
 * Miscellaneous utility functions
 */

private val NOTE_NAMES =
    arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

// Frequency corresponding to A above middle C on a piano (Stuttgart pitch)
const val A440 = 440.0
const val EPSILON = 1e-5

/**
 * Get the note value that corresponds to a given frequency
 * @param freq Input frequency
 * @param A4 The frequency that corresponds to A4 (440 by default)
 * @return Note value (un-rounded)
 */
fun getNoteValue(freq: Double, A4: Double = A440): Double {
    return if (freq <= 0) return 0.0 else 69.0 + 12.0 * log2(freq / A4)
}

/**
 * Get the name of a note given its index
 * @param noteIndex Note index (midi note % 12)
 * @return Note name or "N/A" if out-of-bounds
 */
fun indexToNoteName(noteIndex: Int): String {
    return if (noteIndex >= 0 && noteIndex < NOTE_NAMES.size) NOTE_NAMES[noteIndex] else "N/A"
}

/**
 * Get the name of a note given its value
 * @param note Note value (12 * octave + note index)
 * @return Note name including the octave
 */
fun getNoteName(note: Int): String {
    return indexToNoteName(note % 12) + (note / 12 - 1)
}

/**
 * Check if a note is a flat vs. a sharp
 * @param note Note value
 * @return True if note is sharp
 */
fun isNoteSharp(note: Double): Boolean {
    return indexToNoteName(note.toInt() % 12).contains("#")
}

/**
 * Given an amplitude, get the decibel value
 * @param amp Amplitude (0 to 1)
 * @return Decibels (-160 to 0)
 */
fun getDecibels(amp: Double): Double {
    val dB = amp.pow(2.0)
    return if (dB <= 0) (-160).toDouble() // Default minimum decibels
    else 10.0 * log10(dB)
}

/**
 * Given a decibel value, get the amplitude
 * @param dB Decibels (-160 to 0)
 * @return Amplitude (0 to 1)
 */
fun getAmplitude(dB: Double): Double {
    return sqrt(10.0.pow(dB / 10))
}

/**
 * Round a value to the nearest multiple of X
 * @param value Value to round
 * @param nearest Nearest multiple
 * @return Rounded value
 */
fun roundToNearest(value: Double, nearest: Double): Double {
    return round(value / nearest).toInt() * nearest
}

/**
 * Uppercase the first character in a string
 * @param str String
 * @return String with uppercase first letter
 */
fun upperFirst(str: String?): String? {
    return if (str == null || str.isEmpty())
        str
    else
        Character.toUpperCase(str[0]).toString() + str.substring(1)
}