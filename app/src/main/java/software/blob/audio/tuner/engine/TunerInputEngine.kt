package software.blob.audio.tuner.engine

/**
 * Controls the tuner audio input reader
 */
class TunerInputEngine {

    private val ptr: Long = createEngine()
    private var _active = false

    val active: Boolean get() {
        return _active
    }

    /**
     * Stop the engine when finalized
     */
    fun finalize() = stop()

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

        @JvmStatic
        external fun createEngine(): Long

        @JvmStatic
        external fun setParameters(ptr: Long,
                                   bufferSize: Float,
                                   minAmplitude: Float,
                                   maxFrequency: Float): Boolean

        @JvmStatic
        external fun startEngine(ptr: Long,
                                 deviceId: Int,
                                 channels: Int,
                                 sampleRate: Int): Int

        @JvmStatic
        external fun stopEngine(ptr: Long): Int

        @JvmStatic
        external fun queryFrequency(ptr: Long): Float

        @JvmStatic
        external fun getSampleBuffer(ptr: Long, buf: FloatArray)
    }
}