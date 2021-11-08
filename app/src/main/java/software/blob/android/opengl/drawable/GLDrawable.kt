package software.blob.android.opengl.drawable

import software.blob.android.opengl.shader.GLShader

/**
 * Interface for OpenGL drawable objects
 */
interface GLDrawable {

    /**
     * Draw this object using the given shader
     * @param shader Shader program
     */
    fun draw(shader: GLShader)
}