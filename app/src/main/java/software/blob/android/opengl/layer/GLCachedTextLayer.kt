package software.blob.android.opengl.layer

import software.blob.android.opengl.drawable.GLTextRect

/**
 * Layer used for rendering 2D text
 *
 * For performance reasons, this layer utilizes a cache of [GLTextRect] objects which are mapped
 * based on their text content (or a key)
 *
 * Simply call [get] to create/get a new rectangle with the given text
 */
class GLCachedTextLayer : GLBasic2DLayer() {

    private val map = HashMap<String, GLTextRect>()

    /**
     * Get/create a text rectangle with the given text
     * @param text Text to lookup
     * @return Text rectangle
     */
    operator fun get(text: String): GLTextRect {
        return map.computeIfAbsent(text) { GLTextRect(text).also { add(it) } }
    }

    /**
     * Clear the entire cache
     */
    override fun clear() {
        super.clear()
        map.clear()
    }

    /**
     * Set the visibility of all the text objects
     * This is useful for hiding text that is not used during a render pump
     */
    fun setTextVisible(visible: Boolean) {
        for (o in map.values)
            o.visible = visible
    }
}