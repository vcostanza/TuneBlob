package software.blob.android.thread

/**
 * A looping thread that executes at a steady interval
 * @param interval The interval in milliseconds
 */
abstract class IntervalThread(private val interval: Long) : Thread() {

    private var active = false
    private var runCondition: () -> Boolean = { active }
    private var time = 0L

    /**
     * Create a new [IntervalThread] with a steady frame rate
     * @param frameRate Frames per second
     */
    constructor(frameRate: Int) : this(frameRateToInterval(frameRate))

    /**
     * Start the thread
     */
    override fun start() {
        active = true
        super.start()
    }

    /**
     * Signal the end of the thread (because [stop] is reserved)
     */
    fun end() {
        active = false
    }

    /**
     * Run logic at a steady interval
     */
    override fun run() {
        time = System.currentTimeMillis()
        while (runCondition()) {

            // Execute tasks
            onInterval()

            // Sleep enough to maintain a somewhat constant interval
            try {
                val t = System.currentTimeMillis()
                val sleepTime = interval - (t - time)
                if (sleepTime > 0)
                    sleep(sleepTime)
                time = t
            } catch (ignore: Exception) {
            }
        }
    }

    /**
     * Sub-classes should override this method containing the work that needs to be done
     */
    abstract fun onInterval()

    /**
     * Set the condition required for this thread to continue running
     * This condition will be checked once per interval
     */
    fun setRunCondition(cond: () -> Boolean) {
        runCondition = cond
    }

    companion object {
        fun frameRateToInterval(frameRate: Int) = ((1f / frameRate.toDouble()) * 1000).toLong()
    }
}