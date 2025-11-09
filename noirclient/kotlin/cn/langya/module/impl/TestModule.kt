package cn.langya.module.impl

import cn.langya.event.Render2DEvent
import cn.langya.module.Module
import cn.langya.util.render.RoundedRect
import com.darkmagician6.eventapi.EventTarget
import java.awt.Color

/**
 * @author LangYa466
 * @date 8/11/2025
 */
class TestModule : Module("Test") {
    val test = RoundedRect()

    init {
        enabled = true // test
        // println("test module i")
    }

    @EventTarget
    fun onRender2D(e: Render2DEvent) {
        test.draw(5F, 85F, 50F, 50F, 6F, Color.white)
    }
}