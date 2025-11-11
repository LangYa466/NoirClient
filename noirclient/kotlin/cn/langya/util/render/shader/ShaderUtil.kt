package cn.langya.util.render.shader

import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL20.*
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * @author LangYa466
 * @date 12/11/2025
 */
object ShaderUtil {
    fun createShader(vertexPath: String, fragmentPath: String): Int {
        val program = glCreateProgram()

        val vertexShader = loadShader(vertexPath, GL_VERTEX_SHADER)
        if (vertexShader == 0) {
            glDeleteProgram(program)
            return 0
        }

        val fragmentShader = loadShader(fragmentPath, GL_FRAGMENT_SHADER)
        if (fragmentShader == 0) {
            glDeleteShader(vertexShader)
            glDeleteProgram(program)
            return 0
        }

        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Could not link shader program!")
            System.err.println(glGetProgramInfoLog(program, 512))

            glDeleteProgram(program)
            glDeleteShader(vertexShader)
            glDeleteShader(fragmentShader)
            return 0
        }

        glDetachShader(program, vertexShader)
        glDetachShader(program, fragmentShader)

        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

        return program
    }

    private fun loadShader(path: String, type: Int): Int {
        val source = javaClass.getResourceAsStream("/assets/minecraft/$path")?.use {
            BufferedReader(InputStreamReader(it)).readText()
        } ?: throw RuntimeException("Shader file not found: $path")

        val shader = glCreateShader(type)
        glShaderSource(shader, source)
        glCompileShader(shader)

        glGetShaderi(shader, GL_COMPILE_STATUS)
        return shader
    }

    fun use(program: Int) = glUseProgram(program)
    fun stop() = glUseProgram(0)
}
