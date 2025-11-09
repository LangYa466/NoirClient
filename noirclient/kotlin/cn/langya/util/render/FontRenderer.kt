package cn.langya.util.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.RenderingHints
import java.awt.image.BufferedImage

/**
 * @author LangYa466
 * @date 10/11/2025
 */
object FontRenderer {
    data class CharData(val texture: DynamicTexture, val width: Int, val height: Int)
    const val SCALE_FACTOR = 2F
    var font = Font("Microsoft YaHei", Font.PLAIN, 18) // TODO: FontManager

    private val charDataMap: MutableMap<Char, CharData> = mutableMapOf()
    val fontMetrics: FontMetrics by lazy {
        val g2d = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        g2d.font = font
        val metrics = g2d.fontMetrics
        g2d.dispose()
        metrics
    }

    private fun generateCharTexture(char: Char): CharData {
        val charWidth = fontMetrics.charWidth(char).coerceAtLeast(1)
        val charHeight = fontMetrics.height.coerceAtLeast(1)

        val bufferedImage = BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d = bufferedImage.createGraphics()

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        g2d.font = font
        g2d.color = Color.WHITE

        g2d.drawString(char.toString(), 0, fontMetrics.ascent)
        g2d.dispose()

        val charData = CharData(DynamicTexture(bufferedImage), charWidth, charHeight)
        charDataMap[char] = charData
        return charData
    }

    private fun drawTexture(x: Float, y: Float, width: Float, height: Float) {
        val t = Tessellator.getInstance()
        val w = t.worldRenderer

        w.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        w.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(0.0, 1.0).endVertex()
        w.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(1.0, 1.0).endVertex()
        w.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
        w.pos(x.toDouble(), y.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
        t.draw()
    }

    fun getFontHeight(): Float {
        return fontMetrics.height / SCALE_FACTOR
    }

    fun getStringWidth(text: String): Float {
        var width = 0f
        for (char in text) {
            val charData = charDataMap[char] ?: generateCharTexture(char)
            width += charData.width / SCALE_FACTOR
        }
        return width
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, color: Int): Float {
        // dark
        val shadowColor = (color and -16777216) or ((color and 16579836) shr 2)
        drawString(text, x + .5f, y, shadowColor)
        return drawString(text, x, y, color)
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, color: Color): Float {
        return drawStringWithShadow(text, x, y, color.rgb)
    }

    fun drawCenteredString(text: String, x: Float, y: Float, color: Color): Float {
        val startX = x - getStringWidth(text) / 2
        return drawString(text, startX, y, color.rgb)
    }

    fun drawCenteredString(text: String, x: Float, y: Float, color: Int): Float {
        return drawCenteredString(text, x, y, Color(color, true))
    }

    fun drawCenteredStringWithShadow(text: String, x: Float, y: Float, color: Color): Float {
        val startX = x - getStringWidth(text) / 2
        return drawStringWithShadow(text, startX, y, color.rgb)
    }

    fun drawCenteredStringWithShadow(text: String, x: Float, y: Float, color: Int): Float {
        return drawCenteredStringWithShadow(text, x, y, Color(color, true))
    }

    fun drawString(text: String, x: Float, y: Float, color: Int): Float {
        return drawString(text, x, y, Color(color, true))
    }

    fun drawString(text: String, x: Float, y: Float, color: Color) : Float{
        var currentX = x
        var currentY = y
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.enableTexture2D()

        RenderUtil.color(color)

        for (char in text) {
            if (char == '\r') currentX = x // TODO: reset 没测试 应该可以 没考虑状态前面渲染的会多渲染一个char\r后的width? 反正也不用/r 以后用了再写完整

            if (char == '\n') currentY += getFontHeight() // newline

            val charData = charDataMap[char] ?: generateCharTexture(char)

            val onScreenWidth = charData.width / SCALE_FACTOR
            val onScreenHeight = charData.height / SCALE_FACTOR

            GlStateManager.bindTexture(charData.texture.glTextureId)
            // 不懂啥原理 但是gemini让我加上就对了
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)

            drawTexture(currentX, currentY, onScreenWidth, onScreenHeight)

            currentX += onScreenWidth
        }

        GlStateManager.disableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()

        return currentX
    }
}