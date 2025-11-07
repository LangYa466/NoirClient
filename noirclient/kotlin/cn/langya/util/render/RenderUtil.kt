package cn.langya.util.render

import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * @author LangYa466
 * @date 8/11/2025
 */
object RenderUtil {
    fun color(color: Color) {
        color(color.rgb)
    }

    fun color(color: Int) {
        val a = (color shr 24 and 0xFF) / 255.0f
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        GL11.glColor4f(r, g, b, a)
    }
}