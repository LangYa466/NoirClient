package cn.langya.util.render

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.*

class G2DRoundedRect {
    private var lastWidth = -1f
    private var lastHeight = -1f
    private var lastRadius = -1f
    private var tex: DynamicTexture? = null

    fun draw(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        draw(x, y, width, height, radius, color.rgb)
    }

    fun draw(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
        var radius = radius
        if (width <= 0 || height <= 0) {
            return
        }

        if (radius < 0) {
            radius = 0f
        }

        val clampedRadius = min(radius, min(width, height) / 2.0f)

        if (shapeChanged(width, height, clampedRadius)) {
            updateProperties(width, height, clampedRadius)
            createSDFTextureSuperSampled(width, height, clampedRadius)
        }

        if (this.tex != null) {
            renderTexture(x, y, width, height, color)
        }
    }

    private fun shapeChanged(width: Float, height: Float, radius: Float): Boolean {
        return this.lastWidth != width || this.lastHeight != height || this.lastRadius != radius
    }

    private fun updateProperties(width: Float, height: Float, radius: Float) {
        this.lastWidth = width
        this.lastHeight = height
        this.lastRadius = radius
    }

    private fun signedDistance(pX: Double, pY: Double, boxX: Double, boxY: Double, radius: Double): Double {
        val qX = abs(pX) - boxX + radius
        val qY = abs(pY) - boxY + radius
        val unsignedDistX = max(qX, 0.0)
        val unsignedDistY = max(qY, 0.0)
        val signedDist = min(max(qX, qY), 0.0)
        return sqrt(unsignedDistX * unsignedDistX + unsignedDistY * unsignedDistY) + signedDist - radius
    }

    private fun smoothstep(edge0: Double, edge1: Double, x: Double): Double {
        val t = max(0.0, min(1.0, (x - edge0) / (edge1 - edge0)))
        return t * t * (3.0 - 2.0 * t)
    }

    private fun createSDFTextureSuperSampled(width: Float, height: Float, radius: Float) {
        val intWidth = ceil(width.toDouble()).toInt()
        val intHeight = ceil(height.toDouble()).toInt()

        val bufferedImage = BufferedImage(intWidth, intHeight, BufferedImage.TYPE_INT_ARGB)

        val halfWidth = width / 2.0
        val halfHeight = height / 2.0

        val samples = 2
        val sampleCount = samples * samples
        val step = 1.0 / samples
        val offset = step / 2.0

        for (y in 0..<intHeight) {
            for (x in 0..<intWidth) {
                var totalAlpha = 0.0
                // 采样
                for (sY in 0..<samples) {
                    for (sX in 0..<samples) {
                        // 子采样点精确坐标
                        val subPixelX = x + sX * step + offset
                        val subPixelY = y + sY * step + offset

                        val dist = signedDistance(
                            subPixelX - halfWidth,
                            subPixelY - halfHeight,
                            halfWidth,
                            halfHeight,
                            radius.toDouble()
                        )
                        totalAlpha += smoothstep(1.0, -1.0, dist)
                    }
                }


                val averageAlpha = totalAlpha / sampleCount

                val alphaByte = (averageAlpha * 255).toInt()
                val pixelColor = (alphaByte shl 24) or 0x00FFFFFF
                bufferedImage.setRGB(x, y, pixelColor)
            }
        }

        if (this.tex != null) this.tex!!.deleteGlTexture()

        this.tex = DynamicTexture(bufferedImage)
    }

    private fun renderTexture(x: Float, y: Float, width: Float, height: Float, color: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)

        RenderUtil.color(color)

        GlStateManager.enableTexture2D()
        glBindTexture(GL_TEXTURE_2D, this.tex!!.getGlTextureId())

        Gui.drawModalRectWithCustomSizedTexture(
            x.toInt(),
            y.toInt(),
            0f,
            0f,
            width.toInt(),
            height.toInt(),
            width,
            height
        )

        GlStateManager.disableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }
}