package software.blob.android.opengl.layer

import android.opengl.GLES30.*
import software.blob.android.opengl.shader.GLBasicShader
import software.blob.android.opengl.shader.GLShader

/**
 * Layer that utilizes [GLBasicShader] for drawing objects
 */
abstract class GLBasicLayer : GLLayer() {

    val projectionMatrix = FloatArray(16)

    /**
     * Draw the objects in this layer using a [GLBasicShader]
     * @param shader Basic shader
     */
    override fun drawObjects(shader: GLShader) {
        if (shader !is GLBasicShader) return

        // Link projection matrix
        glUniformMatrix4fv(shader.projectionIdx, 1, false, projectionMatrix, 0)

        // Draw shapes
        glEnableVertexAttribArray(shader.vertexIdx)
        for (o in this)
            o.draw(shader)
        glDisableVertexAttribArray(shader.vertexIdx)
    }
}