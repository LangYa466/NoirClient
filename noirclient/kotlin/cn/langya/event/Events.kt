package cn.langya.event

import com.darkmagician6.eventapi.events.Event
import net.minecraft.client.gui.ScaledResolution

/**
 * @author LangYa466
 * @date 9/11/2025
 */
class UpdateEvent : Event

data class Render2DEvent(val ticks: Float,val sr: ScaledResolution): Event