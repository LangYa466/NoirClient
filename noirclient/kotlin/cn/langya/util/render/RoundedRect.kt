package cn.langya.util.render

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

class RoundedRect {
    private var lastX = -1.0
    private var lastY = -1.0
    private var lastWidth = -1.0
    private var lastHeight = -1.0
    private var lastRadius = -1.0
    private var color: Int = 0
    private var tex: DynamicTexture? = null

    fun draw(x: Double, y: Double, width: Double, height: Double, radius: Double, color: Int) {
        this.color = color

        if (propertiesChanged(x, y, width, height, radius)) {
            updateProperties(x, y, width, height, radius)
            createTexture(width, height, radius)
        }

        tex?.let {
            renderTexture(x, y, width, height)
        }
    }

    private fun propertiesChanged(x: Double, y: Double, width: Double, height: Double, radius: Double): Boolean {
        return lastX != x || lastY != y || lastWidth != width || lastHeight != height || lastRadius != radius
    }

    private fun updateProperties(x: Double, y: Double, width: Double, height: Double, radius: Double) {
        lastX = x
        lastY = y
        lastWidth = width
        lastHeight = height
        lastRadius = radius
    }

    private fun createTexture(width: Double, height: Double, radius: Double) {
        val bufferedImage = BufferedImage(width.toInt(), height.toInt(), BufferedImage.TYPE_INT_ARGB)
        val g = bufferedImage.createGraphics() as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.color = Color.WHITE
        g.fillRoundRect(0, 0, width.toInt(), height.toInt(), radius.toInt(), radius.toInt())
        tex = DynamicTexture(bufferedImage)
    }

    private fun renderTexture(x: Double, y: Double, width: Double, height: Double) {
        GL11.glPushMatrix()

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        RenderUtil.color(color)

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex!!.glTextureId)

        Gui.drawModalRectWithCustomSizedTexture(
            x.toInt(),
            y.toInt(),
            0f,
            0f,
            width.toInt(),
            height.toInt(),
            width.toFloat(),
            height.toFloat()
        )

        GL11.glPopMatrix()
    }
}
