package software.blob.audio.tuner.engine

/**
 * Controls the tuner audio input reader
 */
class TunerInputEngine {

    private val ptr: Long = createEngine()
    private var _active = false

    val active: Boolean get() = _active

    /**
     * Destroy the engine when finalized
     */
    fun finalize() = destroyEngine(ptr)

    /**
     * Set the frequency scanner parameters
     * This cannot be called while the engine is running
     * @param bufferSize Buffer size in seconds (should be under 1 second)
     * @param minAmplitude Minimum scan amplitude
     * @param maxFrequency Maximum scan frequency
     * @return True if set successfully, false if the engine is currently running
     */
    fun setParameters(bufferSize: Float, minAmplitude: Float, maxFrequency: Float): Boolean
        = setParameters(ptr, bufferSize, minAmplitude, maxFrequency)

    /**
     * Start the tuner input engine
     * @param deviceId Input device ID
     * @param channels Number of channels
     * @param sampleRate Sample rate in hertz
     * @return True if started successfully
     */
    fun start(deviceId: Int, channels: Int, sampleRate: Int): Boolean {
        if (startEngine(ptr, deviceId, channels, sampleRate) == 0) {
            _active = true
            return true
        }
        return false
    }

    /**
     * Stop the input engine
     * @return True if stopped successfully
     */
    fun stop(): Boolean {
        if (_active) {
            _active = false
            return stopEngine(ptr) == 0
        }
        return false
    }

    /**
     * Query frequency from the engine
     * @return Frequency in hertz
     */
    fun queryFrequency(): Float = queryFrequency(ptr)

    /**
     * Gets a copy of the current sample buffer
     * @param buf Array to store samples
     */
    fun getSampleBuffer(buf: FloatArray) = getSampleBuffer(ptr, buf)

    companion object {

        init {
            System.loadLibrary("tuner")
        }

        /**
         * Create a new tuner input engine in native
         * @return Pointer address to new native engine
         */
        @JvmStatic
        external fun createEngine(): Long

        /**
         * Set the frequency scanner parameters
         * This cannot be called while the engine is running
         * @param ptr Engine pointer
         * @param bufferSize Buffer size in seconds (should be under 1 second)
         * @param minAmplitude Minimum scan amplitude
         * @param maxFrequency Maximum scan frequency
         * @return True if set successfully, false if the engine is currently running
         */
        @JvmStatic
        external fun setParameters(ptr: Long,
                                   bufferSize: Float,
                                   minAmplitude: Float,
                                   maxFrequency: Float): Boolean

        /**
         * Start the native engine
         * @param ptr Engine pointer
         * @param deviceId Input device ID
         * @param channels Number of channels
         * @param sampleRate Sample rate in hertz
         * @return Error code (0 = success)
         */
        @JvmStatic
        external fun startEngine(ptr: Long,
                                 deviceId: Int,
                                 channels: Int,
                                 sampleRate: Int): Int

        /**
         * Stop the native engine
         * @param ptr Engine pointer
         * @return Error code (0 = success)
         */
        @JvmStatic
        external fun stopEngine(ptr: Long): Int

        /**
         * Destroy the native engine
         * This should only be called during [finalize]
         * @param ptr Engine pointer
         */
        @JvmStatic
        external fun destroyEngine(ptr: Long)

        /**
         * Query frequency from the native engine
         * @param ptr Engine pointer
         * @return Frequency in hertz
         */
        @JvmStatic
        external fun queryFrequency(ptr: Long): Float

        /**
         * Gets a copy of the current sample buffer
         * @param ptr Engine pointer
         * @param buf Array to store samples
         */
        @JvmStatic
        external fun getSampleBuffer(ptr: Long, buf: FloatArray)
    }
}