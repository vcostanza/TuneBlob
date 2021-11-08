package software.blob.android.opengl.layer

import android.opengl.Matrix

/**
 * Layer for rendering 2D objects
 */
open class GLBasic2DLayer : GLBasicLayer() {

    /**
     * Set the viewport (screen size) used by the shader
     * @param width Width in pixels
     * @param height Height in pixels
     */
    override fun setViewport(width: Int, height: Int) {
        val w2 = width.toFloat() / 2f
        val h2 = height.toFloat() / 2f
        Matrix.orthoM(projectionMatrix, 0, -w2, w2, -h2, h2, 0f, 1f)
        Matrix.scaleM(projectionMatrix, 0, 1f, -1f, 1f)
        Matrix.translateM(projectionMatrix, 0, -w2, -h2, 0f)
        super.setViewport(width, height)
    }
}