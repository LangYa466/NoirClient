package cn.langya.config

import cn.langya.module.ModuleManager
import cn.langya.module.value.BooleanValue
import cn.langya.module.value.NumberValue
import cn.langya.module.value.StringValue
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * @author LangYa
 * @since 2023/10/2025
 */
object ModuleConfig : Config("modules.json") {

    override fun saveConfig(): JsonObject {
        val o = JsonObject()

        for (module in ModuleManager.modules.values) {
            val moduleObject = JsonObject()

            moduleObject.addProperty("enabled", module.enabled)

            val valuesObject = JsonObject()
            for (value in module.values) {
                when (value) {
                    // 写三1行就不行 猎奇
                    is NumberValue -> valuesObject.addProperty(value.name, value.value)
                    is BooleanValue -> valuesObject.addProperty(value.name, value.value)
                    is StringValue -> valuesObject.addProperty(value.name, value.value)
                }
            }

            moduleObject.add("values", valuesObject)

            o.add(module.name, moduleObject)
        }

        return o
    }

    override fun loadConfig(o: JsonObject) {
        for (module in ModuleManager.modules.values) {
            if (o.has(module.name)) {
                val moduleObject = o.getAsJsonObject(module.name)

                if (moduleObject.has("enabled")) module.enabled = moduleObject.get("enabled").asBoolean

                if (moduleObject.has("values")) {
                    val valuesObject = moduleObject.getAsJsonObject("values")

                    for (value in module.values) {
                        if (valuesObject.has(value.name)) {
                            val theValue: JsonElement = valuesObject.get(value.name)
                            when (value) {
                                is NumberValue -> value.value = theValue.asFloat
                                is BooleanValue -> value.value = theValue.asBoolean
                                is StringValue -> value.value = theValue.asString
                            }
                        }
                    }
                }
            }
        }
    }
}
