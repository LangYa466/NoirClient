package cn.langya.module.impl

import cn.langya.event.Render2DEvent
import cn.langya.module.Module
import cn.langya.module.value.BooleanValue
import cn.langya.util.render.FontRenderer
import cn.langya.util.render.RoundedRect
import com.darkmagician6.eventapi.EventTarget
import java.awt.Color

/**
 * @author LangYa466
 * @date 8/11/2025
 */
class TestModule : Module("Test") {
    val test = RoundedRect()

    val testBoolean = addSetting("TestBoolean", true)

    init {
        enabled = true // test
        // println("test module i")
    }

    @EventTarget
    fun onRender2D(e: Render2DEvent) {
        //val text = "US 美利坚 \n value\r ${testBoolean.value} "
        val text = "US 美利坚"
        test.draw(
            85F - 2,
            85F - 2,
            FontRenderer.getStringWidth(text) + 4,
            FontRenderer.getFontHeight() + 4,
            6F,
            Color.white
        )
        FontRenderer.drawStringWithShadow(text, 85F, 85F, Color.black)
    }
}