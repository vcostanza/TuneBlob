package software.blob.audio.tuner.gl.layer

import android.graphics.Color
import android.opengl.Matrix
import software.blob.android.opengl.layer.GLBasic2DLayer
import software.blob.audio.tuner.gl.drawable.GLNoteCurve
import software.blob.android.opengl.shader.GLShader

/**
 * Layer used to render [GLNoteCurve] objects
 */
class GLNoteCurveLayer : GLBasic2DLayer() {

    /**
     * Transforms note samples to screen coordinates
     */
    private val noteMatrix = FloatArray(16)

    /**
     * Copy of the 2D projection matrix created by [setViewport]
     */
    private val screenMatrix = FloatArray(16)

    /**
     * The color of the curves
     */
    var color = Color.RED

    /**
     * Setup the note matrix based on a set of parameters
     * @param minTime The time in seconds at x = 0
     * @param minNote The note value at y = height
     * @param pixelsPerSecond The number of pixels per second
     * @param pixelsPerNote The number of pixels per note
     */
    fun setNoteMatrix(minTime: Double, minNote: Double,
                      pixelsPerSecond: Double, pixelsPerNote: Double) {
        Matrix.setIdentityM(noteMatrix, 0)
        Matrix.translateM(noteMatrix, 0, 0f, height.toFloat(), 0f)
        Matrix.scaleM(noteMatrix, 0, pixelsPerSecond.toFloat(), -pixelsPerNote.toFloat(), 1f)
        Matrix.translateM(noteMatrix, 0, -minTime.toFloat(), -minNote.toFloat(), 0f)
        calculateProjectionMatrix()
    }

    /**
     * Set the viewport (screen size) used by the shader
     * @param width Width in pixels
     * @param height Height in pixels
     */
    override fun setViewport(width: Int, height: Int) {
        super.setViewport(width, height)

        // Copy the projection matrix the 2D layer just computed into our screen matrix
        System.arraycopy(projectionMatrix, 0, screenMatrix, 0, 16)

        // Recalculate the projection matrix by multiplying in the note matrix
        calculateProjectionMatrix()
    }

    /**
     * Draw all the curves on this layer
     * @param shader Shader program to use
     */
    override fun drawObjects(shader: GLShader) {
        // Make sure the curves are colored properly before drawing
        for (o in this) {
            if (o is GLNoteCurve)
                o.color = color
        }

        // Draw curves
        super.drawObjects(shader)
    }

    /**
     * Recalculates the projection matrix by multiplying [noteMatrix] and [screenMatrix]
     */
    private fun calculateProjectionMatrix() {
        Matrix.multiplyMM(projectionMatrix, 0, screenMatrix, 0, noteMatrix, 0)
    }
}