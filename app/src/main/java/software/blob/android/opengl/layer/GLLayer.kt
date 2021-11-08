package software.blob.android.opengl.layer

import android.opengl.GLES30.*
import software.blob.android.opengl.shader.GLShader
import software.blob.android.opengl.drawable.GLDrawable

/**
 * A set of [GLDrawable] objects that are drawn as part of a layer
 */
abstract class GLLayer : ArrayList<GLDrawable>(), GLDrawable {

    var visible = true
    var width = 0
    var height = 0

    /**
     * Set the viewport (screen size) used by this layer
     * @param width Width in pixels
     * @param height Height in pixels
     */
    open fun setViewport(width: Int, height: Int) {
        this.width = width
        this.height = height
        glViewport(0, 0, width, height)
    }

    /**
     * Draw this layer
     * @param shader The shader to use for drawing
     */
    override fun draw(shader: GLShader) {

        // If there's nothing to draw then there's nothing to do
        if (!visible || isEmpty()) return

        // Use the shader program
        shader.useProgram()

        // Draw the objects in this layer
        drawObjects(shader)
    }

    /**
     * Draw the objects in this layer
     * @param shader The shader to use for drawing
     */
    abstract fun drawObjects(shader: GLShader)
}