package cn.langya.util.render

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import java.awt.Color


object RenderUtil {
    fun color(color: Color) {
        color(color.rgb)
    }

    fun color(color: Int) {
        val a = (color shr 24 and 0xFF) / 255.0f
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        GlStateManager.color(r, g, b, a)
    }

    fun enableGL2D() {
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)

        // RIMUOVI: GlStateManager.disableTexture2D()
        // Invece, assicuriamoci che sia abilitato per il nostro quad
        GlStateManager.enableTexture2D()
    }

    fun disableGL2D() {
        // Il ripristino qui è corretto, ma lo stato iniziale ora è diverso
        GlStateManager.disableTexture2D() // Ora lo disabilitiamo dopo aver finito
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
    }

    fun drawTexturedQuad(x: Float, y: Float, width: Float, height: Float) {
        glBegin(GL_QUADS)
        glTexCoord2f(0f, 1f)
        glVertex2f(x, y + height)
        glTexCoord2f(1f, 1f)
        glVertex2f(x + width, y + height)
        glTexCoord2f(1f, 0f)
        glVertex2f(x + width, y)
        glTexCoord2f(0f, 0f)
        glVertex2f(x, y)
        glEnd()
    }
}