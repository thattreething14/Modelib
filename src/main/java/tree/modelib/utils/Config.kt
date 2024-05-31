package tree.modelib.utils

import org.bukkit.configuration.file.FileConfiguration
import java.io.File


object Config {
    var useDisplayEntitiesWhenPossible: Boolean = false
    var scale: Float = 1.0F // Default scale size of models
    var frameRate: Int = 20 // Framerate of animations
    fun initializeConfig() {
        val file: File = DirectoryHelper.fileCreator("config.yml")
        val fileConfiguration: FileConfiguration = DirectoryHelper.fileConfigurationCreator(file)!!
        useDisplayEntitiesWhenPossible = DirectoryHelper.setBoolean(
            listOf(
                "Sets whether display entities will be used over armor stands.",
                "It is not always possible to use display entities as they do not exist for bedrock, nor do they exist for servers older than 1.19.4.",
                "plugin automatically falls back to armor stand displays when it's not possible to use display entities!"
            ),
            fileConfiguration, "useDisplayEntitiesWhenPossible", true
        )
        scale = DirectoryHelper.setFloat(
            listOf(
                "Sets the scale of models.",
                "Currently, highly recommended to keep it at 1.0 or else animations and models will not load properly."),
            fileConfiguration,
            "scale", 1.0F
        )
        frameRate = DirectoryHelper.setInt(
            listOf(
                "Sets the framerate cap for all animations--however also determined by tps.",
                "Recommended to keep it at 20 fps for best performance."
            ),
            fileConfiguration, "frameRate", 20
        )
        DirectoryHelper.fileSaverOnlyDefaults(fileConfiguration, file)
    }
}