import net.minecraft.client.main.ClientInitializer

object Start {
    @JvmStatic
    fun main(args: Array<String>) {
        val additionalArgs = mapOf(
            // Account-related
            "--username" to "LangYa",
            "--accessToken" to "0",
            "--userProperties" to "{}",

            // Version-related
            "--version" to "1.8.9",
            "--assetIndex" to "1.8",

            // Folder-related
            "--gameDir" to System.getProperty("user.dir"),
            "--assetsDir" to "assets"
        ).flatMap { listOf(it.key, it.value) }.toTypedArray()

        ClientInitializer.main(args + additionalArgs)
    }
}