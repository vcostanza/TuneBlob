package software.blob.android.collections

import java.util.concurrent.ConcurrentLinkedDeque

/**
 * A queue of listener interfaces mapped by a key
 */
class ListenerMap<E> : NonNullMap<String, ConcurrentLinkedDeque<E>>() {

    override fun newEmptyValue(key: String): ConcurrentLinkedDeque<E> {
        return ConcurrentLinkedDeque<E>()
    }
}