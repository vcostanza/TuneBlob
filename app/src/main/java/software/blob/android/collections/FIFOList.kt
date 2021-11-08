package software.blob.android.collections

/**
 * A simple first-in/first-out (FIFO) [ArrayList]
 * @param capacity The limit on the size of this list (adjustable)
 */
open class FIFOList<E>(capacity: Int) : ArrayList<E>(capacity) {

    private var _capacity = capacity

    /**
     * The size limit of the list
     */
    var capacity: Int get() { return _capacity }
        set(value) {
            if (_capacity != value) {
                _capacity = value
                trimToCapacity()
            }
        }

    constructor(capacity: Int, coll: Collection<E>) : this(capacity) {
        addAll(coll)
    }

    /**
     * Add an element to the list
     * @param element Element to add
     * @return True if the element was added
     */
    override fun add(element: E): Boolean {
        if (size == capacity) removeAt(0)
        return super.add(element)
    }

    /**
     * Add an element to the list at the given index
     * @param index Index to add elements
     * @param element Element to add
     */
    override fun add(index: Int, element: E) {
        super.add(index, element)
        trimToCapacity()
    }

    /**
     * Add elements to the list
     * @param elements Elements to add
     * @return True if elements added
     */
    final override fun addAll(elements: Collection<E>): Boolean {
        return super.addAll(elements).also { trimToCapacity() }
    }

    /**
     * Add elements to the list
     * @param index Index to add elements
     * @param elements Elements to add
     * @return True if elements added
     */
    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        return super.addAll(index, elements).also { trimToCapacity() }
    }

    /**
     * Trim the list down to its set [capacity]
     */
    private fun trimToCapacity() {
        if (size > capacity) removeRange(0, size - capacity)
    }
}