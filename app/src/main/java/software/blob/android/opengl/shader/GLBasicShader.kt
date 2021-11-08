package software.blob.android.opengl.shader

import android.opengl.GLES30.*

/**
 * Basic drawing shader for OpenGL that supports colors and textures
 */
open class GLBasicShader(
    vertexShaderSrc: String = VERTEX_SRC,
    fragmentShaderSrc: String = FRAGMENT_SRC
)
    : GLShader(vertexShaderSrc, fragmentShaderSrc) {

    // Shader parameters
    val vertexIdx = glGetAttribLocation(program, "aVertex")
    val projectionIdx = glGetUniformLocation(program, "uProjectionMatrix")
    val transformIdx = glGetUniformLocation(program, "uTransformMatrix")
    val colorIdx = glGetUniformLocation(program, "uColor")

    // Texture data
    val texEnabled = glGetUniformLocation(program, "uTexEnabled")
    val textureIdx = glGetUniformLocation(program, "uTexture")
    val texCoordIdx = glGetAttribLocation(program, "aTexCoordinate")

    companion object {

        // Vertex shader code
        protected const val VERTEX_SRC = "attribute vec4 aVertex;" +
                "uniform mat4 uProjectionMatrix;" +
                "uniform mat4 uTransformMatrix;" +
                "attribute vec2 aTexCoordinate;" +
                "varying vec2 vTexCoordinate;" +
                "void main() {" +
                "   gl_Position = uProjectionMatrix * uTransformMatrix * aVertex;" +
                "   vTexCoordinate = aTexCoordinate;" +
                "}"

        // Fragment shader code
        protected const val FRAGMENT_SRC = "precision mediump float;" +
                "uniform vec4 uColor;" +
                "uniform bool uTexEnabled;" +
                "uniform sampler2D uTexture;" +
                "varying vec2 vTexCoordinate;" +
                "void main() {" +
                "   if (uTexEnabled) {" +
                "       gl_FragColor = uColor * texture2D(uTexture, vTexCoordinate);" +
                "   } else {" +
                "       gl_FragColor = uColor;" +
                "   }" +
                "}"
    }
}