package software.blob.android.preference.util

import android.content.SharedPreferences

/**
 * Simple version of [SharedPreferences.OnSharedPreferenceChangeListener]
 */
fun interface SimplePreferenceListener {

    /**
     * A preference value has been modified
     * @param prefs Preferences object
     * @param key Preference key
     * @param value Preference value
     */
    fun onPreferenceChanged(prefs: SimplePreferences, key: String, value: Any?)
}