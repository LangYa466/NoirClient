package cn.langya.module

import cn.langya.module.impl.TestModule

/**
 * @author LangYa466
 * @date 8/11/2025
 */
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