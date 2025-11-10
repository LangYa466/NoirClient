package cn.langya.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * @author LangYa466
 * @date 10/11/2025
 */
abstract class Config(val fileName: String) {
    var file: File = File(fileName)

    abstract fun saveConfig(): JsonObject
    abstract fun loadConfig(o: JsonObject)
}

class ConfigManager {
    val configs = arrayListOf<Config>()
    private val gson = GsonBuilder().setPrettyPrinting().create()
    val clientDir = File("NoirClient")
    var saveThread = Thread {
        while (true) {
            try {
                Thread.sleep(10000)
                saveAllConfigs()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    init {
        configs.add(ModuleConfig)

        if (!clientDir.exists()) clientDir.mkdir()

        configs.forEach { it.file = File(clientDir, it.file.name) }

        loadAllConfigs()

        saveThread.name = "NoriConfigSaveThread"
        saveThread.isDaemon = true
        saveThread.start()
    }

    fun loadAllConfigs() {
        configs.forEach { loadConfig(it) }
    }

    fun saveAllConfigs() {
        configs.forEach { saveConfig(it) }
    }

    fun loadConfig(config: Config) {
        if (!config.file.exists()) {
            println("Config ${config.fileName} file not found, using default config.")
            return
        }

        try {
            val reader = FileReader(config.file)
            val jsonObject = gson.fromJson(reader, JsonObject::class.java)
            config.loadConfig(jsonObject)
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveConfig(config: Config) {
        try {
            val writer = FileWriter(config.file)
            val jsonObject = config.saveConfig()
            gson.toJson(jsonObject, writer)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}