package software.blob.android.opengl.drawable

import android.graphics.RectF

/**
 * OpenGL rectangle shape
 */
open class GLRectangle(initialCapacity: Int = 6) : GLShape(initialCapacity) {

    /**
     * Add a rectangle to the shape
     * @param left Left side of the rectangle
     * @param top Top side of the rectangle
     * @param right Right side of the rectangle
     * @param bottom Bottom side of the rectangle
     */
    fun addRectangle(left: Float, top: Float, right: Float, bottom: Float) {
        addVertices(floatArrayOf(
            left, top,
            right, bottom,
            right, top,
            left, bottom,
            right, bottom,
            left, top
        ))
    }

    /**
     * Add a rectangle to the shape
     * @param rect Rectangle to add
     */
    fun addRectangle(rect: RectF) {
        addRectangle(rect.left, rect.top, rect.right, rect.bottom)
    }
}