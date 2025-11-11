package cn.langya.util.render.shader

import cn.langya.util.render.RenderUtil
import org.lwjgl.opengl.GL20.*
import java.awt.Color

object RoundedRect {
    private val baseShader by lazy {
        ShaderUtil.createShader("noir/shaders/vertex.vert", "noir/shaders/roundRect.frag")
    }

    fun drawRound(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        RenderUtil.enableGL2D()
        ShaderUtil.use(baseShader)

        glUniform2f(glGetUniformLocation(baseShader, "rectPos"), x, y)
        glUniform2f(glGetUniformLocation(baseShader, "rectSize"), width, height)
        glUniform1f(glGetUniformLocation(baseShader, "radius"), radius)
        glUniform4f(glGetUniformLocation(baseShader, "color"),
            color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        RenderUtil.drawTexturedQuad(x, y, width, height)

        ShaderUtil.stop()
        RenderUtil.disableGL2D()
    }
}