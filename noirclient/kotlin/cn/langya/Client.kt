package cn.langya

import cn.langya.module.ModuleManager

/**
 * @author LangYa466
 * @date 8/11/2025
 */
object Client {
    const val VERSION = "1.0"

    fun init() {
        ModuleManager()
        println(ModuleManager.modules.toString())
    }
}