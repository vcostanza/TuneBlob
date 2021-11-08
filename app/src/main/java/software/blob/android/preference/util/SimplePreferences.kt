package software.blob.android.preference.util

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import software.blob.android.collections.ListenerMap
import software.blob.audio.util.Misc
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * A helper class for [SharedPreferences] that takes advantage of operator overrides
 * and other useful shorthand.
 *
 * This class also supports listeners which are automatically unregistered
 * when the instance is garbage collected.
 */
open class SimplePreferences(protected val context: Context) : Iterable<Any?> {

    /**
     * The underlying [SharedPreferences] interface
     */
    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Listener functionality
     */
    private val listeners = ConcurrentLinkedDeque<SimplePreferenceListener>()
    private val keyListeners = ListenerMap<() -> Unit>()
    private val listenerForwarder = ListenerForwarder(this)

    init {
        prefs.registerOnSharedPreferenceChangeListener(listenerForwarder)
    }

    /**
     * Unregister the listener forwarder when this object is about to GC
     */
    fun finalize() {
        prefs.unregisterOnSharedPreferenceChangeListener(listenerForwarder)
    }

    /**
     * Get a preference value
     * @param key Preference key
     * @return Preference value
     */
    operator fun get(key: String): Any? = prefs.all[key]

    /**
     * Get a preference value
     * @param keyResId Preference key resource ID
     * @return Preference value
     */
    operator fun get(@StringRes keyResId: Int): Any? = get(keyString(keyResId))

    /**
     * Get a preference value
     * @param key Preference key
     * @param def Fallback value
     * @return Preference value as type [T] or [def]
     */
    inline operator fun <reified T> get(key: String, def: T): T {
        // Check if the value is null and return fallback if so
        val v = this[key] ?: return def

        // Check if we can cast the value
        if (v is T) return v

        // Attempt to convert
        val converted = Misc.convert(v, def!!::class)
        return if (converted is T) converted else def
    }

    /**
     * Get a preference value
     * @param keyResId Preference key resource ID
     * @param def Fallback value
     * @return Preference value as type [T] or [def]
     */
    inline operator fun <reified T> get(@StringRes keyResId: Int, def: T): T
        = get(keyString(keyResId), def)

    /**
     * Get an integer preference value
     * @param key Preference key
     * @return Preference value integer
     */
    fun getInt(key: String): Int = this[key, 0]

    /**
     * Get an integer preference value
     * @param keyResId Preference key resource ID
     * @return Preference value integer
     */
    fun getInt(@StringRes keyResId: Int): Int = getInt(keyString(keyResId))

    /**
     * Get a long preference value
     * @param key Preference key
     * @return Preference value long
     */
    fun getLong(key: String): Long = this[key, 0L]

    /**
     * Get a long preference value
     * @param keyResId Preference key resource ID
     * @return Preference value long
     */
    fun getLong(@StringRes keyResId: Int): Long = getLong(keyString(keyResId))

    /**
     * Get a float preference value
     * @param key Preference key
     * @return Preference value float
     */
    fun getFloat(key: String): Float = this[key, 0f]

    /**
     * Get a float preference value
     * @param keyResId Preference key resource ID
     * @return Preference value float
     */
    fun getFloat(@StringRes keyResId: Int): Float = getFloat(keyString(keyResId))

    /**
     * Get a boolean preference value
     * @param key Preference key
     * @return Preference value boolean
     */
    fun getBoolean(key: String): Boolean = this[key, false]

    /**
     * Get a boolean preference value
     * @param keyResId Preference key resource ID
     * @return Preference value boolean
     */
    fun getBoolean(@StringRes keyResId: Int): Boolean = getBoolean(keyString(keyResId))

    /**
     * Get a string preference value
     * @param key Preference key
     * @return Preference value string
     */
    fun getString(key: String): String = this[key, ""]

    /**
     * Get a string preference value
     * @param keyResId Preference key resource ID
     * @return Preference value string
     */
    fun getString(@StringRes keyResId: Int): String = getString(keyString(keyResId))

    /**
     * Get a string set preference value
     * @param key Preference key
     * @return Preference value string set
     */
    fun getStringSet(key: String): Set<String> = this[key, HashSet()]

    /**
     * Get a string set preference value
     * @param keyResId Preference key resource ID
     * @return Preference value string set
     */
    fun getStringSet(@StringRes keyResId: Int): Set<String> = getStringSet(keyString(keyResId))

    /**
     * Set a preference value
     * @param key Preference key
     * @param value Preference value
     */
    operator fun set(key: String, value: Any?) {
        val editor = prefs.edit()
        if (value != null) {
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Double -> editor.putFloat(key, value.toFloat())
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, value as Set<String>)
            }
        } else
            editor.remove(key)
        editor.apply()
    }

    /**
     * Set a preference value
     * @param keyResId Preference key resource ID
     * @param value Preference value
     */
    operator fun set(@StringRes keyResId: Int, value: Any?) = set(keyString(keyResId), value)

    /**
     * Set the default preference value
     * @param key Preference key
     * @param value Preference value
     */
    fun setDefault(key: String, value: Any?) {
        if (key !in this) set(key, value)
    }

    /**
     * Set the default preference value
     * @param keyResId Preference key resource ID
     * @param value Preference value
     */
    fun setDefault(@StringRes keyResId: Int, value: Any?) {
        if (keyResId !in this) set(keyResId, value)
    }

    /**
     * Check if a key is defined in preferences
     * @param key Preference key
     * @return True if defined
     */
    operator fun contains(key: String): Boolean = prefs.contains(key)

    /**
     * Check if a key is defined in preferences
     * @param keyResId Preference key resource ID
     * @return True if defined
     */
    operator fun contains(@StringRes keyResId: Int): Boolean = contains(keyString(keyResId))

    /**
     * Iterator redirect for [SharedPreferences]
     * @return Preference value iterator
     */
    override fun iterator(): Iterator<Any?> = prefs.all.iterator()

    /**
     * Helper method for getting a preference key from a string resource ID
     * @param keyResId Preference resource ID
     * @return Preference key string
     */
    fun keyString(@StringRes keyResId: Int): String = context.getString(keyResId)

    /**
     * Add a preference listener
     * @param listener Preference listener
     */
    fun listen(listener: SimplePreferenceListener) = listeners.add(listener)

    /**
     * Add a preference listener for a given key
     * @param key Preference key
     * @param listener Preference listener
     */
    fun listen(key: String, listener: () -> Unit) = keyListeners[key].add(listener)

    /**
     * Add a preference listener for a given key
     * @param keyResId Preference key resource ID
     * @param listener Preference listener
     */
    fun listen(@StringRes keyResId: Int, listener: () -> Unit)
        = listen(keyString(keyResId), listener)

    /**
     * Add a preference listener for a given key and then run the listener
     * @param key Preference key
     * @param listener Preference listener
     */
    fun listenAndRun(key: String, listener: () -> Unit) {
        listen(key, listener)
        listener()
    }

    /**
     * Add a preference listener for a given key and then run the listener
     * @param keyResId Preference key resource ID
     * @param listener Preference listener
     */
    fun listenAndRun(@StringRes keyResId: Int, listener: () -> Unit)
        = listenAndRun(keyString(keyResId), listener)

    /**
     * Remove a preference listener
     * @param listener Preference listener
     */
    fun unlisten(listener: SimplePreferenceListener) = listeners.remove(listener)

    /**
     * Remove a preference listener for a specific key
     * @param key Preference key
     * @param listener Preference listener
     */
    fun unlisten(key: String, listener: () -> Unit) = keyListeners[key].remove(listener)

    /**
     * Remove a preference listener for a specific key
     * @param keyResId Preference key resource ID
     * @param listener Preference listener
     */
    fun unlisten(@StringRes keyResId: Int, listener: () -> Unit)
        = unlisten(keyString(keyResId), listener)

    /**
     * Tracks preference changes and forwards them to the listeners
     *
     * This class holds a [WeakReference] to the parent instance so the listener can be
     * automatically unregistered when the parent instance is garbage collected.
     */
    private class ListenerForwarder(prefs: SimplePreferences)
        : SharedPreferences.OnSharedPreferenceChangeListener {

        private val prefs: WeakReference<SimplePreferences> = WeakReference(prefs)

        override fun onSharedPreferenceChanged(p: SharedPreferences?, key: String) {
            val sp = prefs.get()
            if (sp != null) {
                val value = sp[key]

                // Fire main preference listeners
                for (l in sp.listeners)
                    l.onPreferenceChanged(sp, key, value)

                // Fire key listeners
                if (key in sp.keyListeners) {
                    for (l in sp.keyListeners[key])
                        l()
                }
            }
        }
    }
}