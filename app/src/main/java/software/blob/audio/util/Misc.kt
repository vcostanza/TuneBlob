package software.blob.audio.util

import android.util.Log
import android.view.View
import android.view.ViewGroup
import java.lang.Exception
import kotlin.collections.HashMap
import kotlin.math.*
import androidx.core.view.children
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

private const val TAG = "Misc"

/**
 * Miscellaneous utility functions
 */
object Misc {

    private val NOTE_NAMES =
        arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val NOTE_TO_SEMITONE: MutableMap<String, Int> = HashMap()

    // Frequency corresponding to A above middle C on a piano (Stuttgart pitch)
    const val A440 = 440.0
    const val EPSILON = 1e-5

    init {
        for (i in NOTE_NAMES.indices) NOTE_TO_SEMITONE[NOTE_NAMES[i]] = i
    }

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
     * Get note value given a note name
     * @param note Note name (note + octave)
     * @return Note value
     */
    fun getNoteValue(note: String?): Int {
        try {
            if (note == null || note.isEmpty()) return 0
            for (i in note.indices) {
                val c = note[i]
                if (c in '0'..'9') {
                    val semitone = NOTE_TO_SEMITONE[note.substring(0, i)]
                    val octave = note.substring(i).toInt()
                    return (octave + 1) * 12 + semitone!!
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse note: $note", e)
        }
        return 0
    }

    /**
     * Get a semitone given a note name
     * @param note Note name (without octave)
     * @return Semitone
     */
    fun getSemitone(note: String): Int? {
        return NOTE_TO_SEMITONE[note]
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

    /**
     * Check if two values are not equal and execute code if so
     * @param v1 Value 1
     * @param v2 Value 2
     * @param r Code to execute if not equal
     * @return Value 2
     */
    inline fun <T> ifNotEqual(v1: T, v2: T, r: () -> Unit): T {
        if (v1 != v2)
            r()
        return v2
    }

    /**
     * Attempt to convert a value to another value of a differing class
     * @param v Value to convert
     * @param clazz Class to attempt to convert to
     * @return Converted value or null if not supported
     */
    fun convert(v: Any?, clazz: KClass<*>): Any? {

        // No conversion needed
        if (v == null || clazz.isInstance(v)) return v

        // If the return type is a string then just call toString
        if (clazz == String::class) return v.toString()

        // Supported conversions between numbers and strings
        try {
            if (clazz == Int::class) {
                return when (v) {
                    is Number -> v.toInt()
                    is String -> v.toInt()
                    else -> null
                }
            } else if (clazz == Long::class) {
                return when (v) {
                    is Number -> v.toLong()
                    is String -> v.toLong()
                    else -> null
                }
            } else if (clazz == Float::class) {
                return when (v) {
                    is Number -> v.toFloat()
                    is String -> v.toFloat()
                    else -> null
                }
            } else if (clazz == Double::class) {
                return when (v) {
                    is Number -> v.toDouble()
                    is String -> v.toDouble()
                    else -> null
                }
            } else if (clazz == Boolean::class && v is String)
                return v.toBoolean()
        } catch (ignored: Exception) {
        }

        // Conversion is not supported
        return null
    }

    /**
     * Find all views that are instances of a given class
     * @param view View group to search
     * @param clazz View class to search for
     * @param outViews Results are stored here
     */
    fun findViewsByClass(view: ViewGroup, clazz: KClass<out View>, outViews: MutableList<View>) {
        for (child in view.children) {
            if (clazz.isInstance(child))
                outViews += child
            if (child is ViewGroup)
                findViewsByClass(child, clazz, outViews)
        }
    }

    /**
     * Find all views that are instances of a given class
     * @param view View group to search
     * @param clazz View class to search for
     * @return View results
     */
    fun findViewsByClass(view: ViewGroup, clazz: KClass<out View>): MutableList<View> {
        val results = ArrayList<View>()
        findViewsByClass(view, clazz, results)
        return results
    }
}