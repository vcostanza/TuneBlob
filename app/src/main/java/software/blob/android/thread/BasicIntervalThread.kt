package software.blob.android.thread

/**
 * An [IntervalThread] that accepts a basic task to perform
 */
class BasicIntervalThread(interval: Long, private val task: () -> Unit) : IntervalThread(interval) {

    /**
     * Frame-rate based constructor
     */
    constructor(frameRate: Int, task: () -> Unit) : this(frameRateToInterval(frameRate), task)

    /**
     * Perform the given task
     */
    override fun onInterval() = task()
}