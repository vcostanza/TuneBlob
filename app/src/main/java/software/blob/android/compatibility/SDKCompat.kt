package software.blob.android.compatibility

import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.annotation.ColorRes

/**
 * Various class method injections for working around minimum SDK requirements
 */
@Suppress("deprecation")
object SDKCompat {

    /**
     * Get the color value for a color resource ID
     * @param id Color resource ID
     * @return Color integer (ARGB)
     */
    fun Context.getColorCompat(@ColorRes id: Int): Int {
        return if (checkVersion(VERSION_CODES.M))
            getColor(id) // Requires API 23+
        else
            resources.getColor(id)
    }

    /**
     * Get the value mapped by a given key
     * If the value does not exist, compute and store it using the mapping function
     * @param key Key
     * @param mappingFunction Mapping function for generating the missing value
     * @return The stored value
     */
    fun <K, V> HashMap<K, V>.computeIfAbsentCompat(key: K, mappingFunction: () -> V): V {
        if (checkVersion(VERSION_CODES.N)) {
            // Use actual computeIfAbsent method in Android 7.0+ (24)
            return computeIfAbsent(key) { mappingFunction() }
        } else {
            // Basic fallback implementation
            var v = get(key)
            if (v != null) return v
            v = mappingFunction()
            put(key, v)
            return v
        }
    }

    /**
     * Shorthand helper method for checking if the current version is greater than or
     * equal to another version
     * @param supportVersion The support version's SDK number
     */
    fun checkVersion(supportVersion: Int) = Build.VERSION.SDK_INT >= supportVersion
}