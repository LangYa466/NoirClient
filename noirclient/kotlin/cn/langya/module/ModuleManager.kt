package cn.langya.module

import cn.langya.module.impl.TestModule
import com.darkmagician6.eventapi.EventManager

/**
 * @author LangYa466
 * @date 8/11/2025
 */
open class Module(val name: String, val description: String = "") {
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