package software.blob.android.opengl.shader

import android.opengl.GLES30.*

/**
 * Simple OpenGL shader helper
 */
open class GLShader(vertexShaderSrc: String, fragmentShaderSrc: String) {

    val program: Int

    init {
        // Compile the shaders
        val vertexShader = compileShader(vertexShaderSrc, GL_VERTEX_SHADER)
        val fragmentShader = compileShader(fragmentShaderSrc, GL_FRAGMENT_SHADER)

        // Create program from the shaders
        program = createProgram(vertexShader, fragmentShader)

        // Delete the individual shaders since they're no longer used
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
    }

    /**
     * Delete the associated program once the object is ready for garbage collection
     */
    fun finalize() {
        glDeleteProgram(program)
    }

    /**
     * Use this program for upcoming draw operations
     */
    fun useProgram() {
        glUseProgram(program)
    }

    /**
     * Get the program info log
     * @return Program log
     */
    fun getProgramLog(): String {
        return glGetProgramInfoLog(program)
    }

    /**
     * Create and compile a shader
     * @param shaderSrc Shader source code
     * @param type Shader type
     * @return Shader ID
     */
    protected fun compileShader(shaderSrc: String, type: Int): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, shaderSrc)
        glCompileShader(shader)
        return shader
    }

    /**
     * Create a shader program from a vertex and fragment shader
     * @param vertexShader Vertex shader ID
     * @param fragmentShader Fragment shader ID
     * @return Shader program ID
     */
    protected fun createProgram(vertexShader: Int, fragmentShader: Int): Int {
        val program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)
        return program
    }
}