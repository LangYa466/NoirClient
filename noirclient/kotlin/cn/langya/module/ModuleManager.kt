package cn.langya.module

import cn.langya.module.impl.TestModule
import cn.langya.module.value.BooleanValue
import cn.langya.module.value.EnumValue
import cn.langya.module.value.StringValue
import cn.langya.module.value.Value
import com.darkmagician6.eventapi.EventManager
import net.minecraft.client.Minecraft

/**
 * @author LangYa466
 * @date 8/11/2025
 */
open class Module(val name: String, val description: String = "") {
    val values = mutableListOf<Value<*>>()
    val mc = Minecraft.getMinecraft()

    /*
    // auto add values
    init {
        for (field in this::class.java.declaredFields) {
            try {
                field.isAccessible = true
                val obj: Any? = field.get(this)
                if (obj is Value<*>) {
                    this.values.add(obj)
                    println("Added value: ${obj}")
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }
     */

    var enabled: Boolean = false
     set(value) {
         if (value) {
             onEnable()
             EventManager.register(this)
         } else {
             onDisable()
             EventManager.unregister(this)
         }

         field = value
     }

    fun onDisable() { }
    fun onEnable() { }

    fun toggle() {
        enabled = !enabled
    }

    fun addSetting(name: String, defaultValue: Boolean): BooleanValue {
        return BooleanValue(name, defaultValue).also { values.add(it) }
    }

    fun addSetting(name: String, defaultValue: String): StringValue {
        return StringValue(name, defaultValue).also { values.add(it) }
    }

    fun <T : Enum<T>> addSetting(name: String, enum: T): Value<*> {
        return EnumValue(name, enum).also { values.add(it) }
    }

    // 得手动as 那算了
    /*
    fun <T> addSetting(name: String, defaultValue: T): Value<*> {
        return when (defaultValue) {
            is Boolean -> BooleanValue(name, defaultValue).also { values.add(it) }
            is String -> StringValue(name, defaultValue)
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

     */
}

class ModuleManager {
    companion object {
        val modules = mutableMapOf<String, Module>()
    }

    fun addModule(module: Module) {
        modules[module.name] = module
    }

    init {
        addModule(TestModule())
    }
}