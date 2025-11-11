package cn.langya.module.impl

import cn.langya.event.Render2DEvent
import cn.langya.module.Module
import cn.langya.util.render.FontRenderer
import cn.langya.util.render.shader.RoundedRect
import com.darkmagician6.eventapi.EventTarget
import java.awt.Color

/**
 * @author LangYa466
 * @date 8/11/2025
 */
class TestModule : Module("Test") {
    val testBoolean = addSetting("TestBoolean", true)

    init {
        enabled = true // test
        // println("test module i")
    }

    @EventTarget
    fun onRender2D(e: Render2DEvent) {
        //val text = "US 美利坚 \n value\r ${testBoolean.value} "
        val text = "US 美利坚"
        RoundedRect.drawRound(
            x = 40f,
            y = 50f,
            width = 200f,
            height = 100f,
            radius = 15f,
            color = Color(0, 150, 255, 200)
        )

        FontRenderer.drawStringWithShadow(text, 85F, 85F, Color.black)
    }
}