package software.blob.android.collections

/**
 * A [HashMap] with non-null values that are automatically created when they are queried
 */
abstract class NonNullMap<K, V> : HashMap<K, V>() {

    /**
     * Create a new empty value for a given key
     * @param key Map key
     * @return New empty value
     */
    abstract fun newEmptyValue(key: K): V

    /**
     * Get the value mapped to the given key
     * Automatically computes a new empty value if none is set
     * @param key Map key
     * @return Value
     */
    override operator fun get(key: K): V {
        // Intentionally doesn't use computeIfAbsent since it calls get(key) which causes
        // an infinite loop on older devices
        var v = super.get(key)
        if (v != null) return v
        v = newEmptyValue(key)
        put(key, v)
        return v
    }
}