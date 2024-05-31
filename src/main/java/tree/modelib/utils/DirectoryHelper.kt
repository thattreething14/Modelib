package tree.modelib.utils

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import tree.modelib.Plugin
import tree.modelib.utils.VersionChecker.serverVersionOlderThan
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


object DirectoryHelper {
    fun fileCreator(path: String, fileName: String?): File {
        val file = fileName?.let { File(Plugin.instance!!.dataFolder.path + "/" + path + "/", it) }
        return fileCreator(file!!)
    }

    fun fileCreator(fileName: String?): File {
        val file = File(Plugin.instance!!.dataFolder.path, fileName.toString())
        return fileCreator(file)
    }

    fun fileCreator(file: File): File {
        if (!file.exists()) try {
            file.parentFile.mkdirs()
            file.createNewFile()
        } catch (ex: IOException) {
            Bukkit.getLogger().warning("Error generating the plugin file: " + file.name)
        }

        return file
    }

    fun directoryCreator(directoryName: String?): File {
        val file = File(Plugin.instance!!.dataFolder.path, directoryName.toString())
        file.mkdir()
        return fileCreator(file)
    }

    fun fileConfigurationCreator(file: File): FileConfiguration? {
        try {
            return YamlConfiguration.loadConfiguration(InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8))
        } catch (exception: Exception) {
            Bukkit.getLogger().warning("Failed to read configuration from file " + file.name)
            return null
        }
    }

    fun fileSaverCustomValues(fileConfiguration: FileConfiguration, file: File?) {
        fileConfiguration.options().copyDefaults(true)

        try {
            fileConfiguration.save(file!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun fileSaverOnlyDefaults(fileConfiguration: FileConfiguration, file: File?) {
        fileConfiguration.options().copyDefaults(true)

        try {
            fileConfiguration.save(file!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setComments(fileConfiguration: FileConfiguration, key: String?, comments: List<String>) {
        if (serverVersionOlderThan(18, 2)) return
        fileConfiguration.setComments(key!!, comments)
    }

    fun setBoolean(fileConfiguration: FileConfiguration, key: String?, defaultValue: Boolean): Boolean {
        fileConfiguration.addDefault(key!!, defaultValue)
        return fileConfiguration.getBoolean(key)
    }

    fun setBoolean(
        comments: List<String>,
        fileConfiguration: FileConfiguration,
        key: String?,
        defaultValue: Boolean
    ): Boolean {
        val value = setBoolean(fileConfiguration, key, defaultValue)
        setComments(fileConfiguration, key, comments)
        return value
    }
    fun setInt(comments: List<String>, fileConfiguration: FileConfiguration, key: String?, defaultValue: Int): Int {
        val value = setInt(fileConfiguration, key, defaultValue)
        setComments(fileConfiguration, key, comments)
        return value
    }
    fun setInt(fileConfiguration: FileConfiguration, key: String?, defaultValue: Int): Int {
        fileConfiguration.addDefault(key!!, defaultValue)
        return fileConfiguration.getInt(key)
    }
    fun setDouble(fileConfiguration: FileConfiguration, key: String?, defaultValue: Double): Double {
        fileConfiguration.addDefault(key!!, defaultValue)
        return fileConfiguration.getDouble(key)
    }
    fun setFloat(comments: List<String>, fileConfiguration: FileConfiguration, key: String?, defaultValue: Float
    ): Float {
        fileConfiguration.addDefault(key!!, defaultValue.toDouble())
        setComments(fileConfiguration, key, comments)
        return fileConfiguration.getDouble(key).toFloat()
    }
    fun setDouble(
        comments: List<String>,
        fileConfiguration: FileConfiguration,
        key: String?,
        defaultValue: Double
    ): Double {
        val value = setDouble(fileConfiguration, key, defaultValue)
        setComments(fileConfiguration, key, comments)
        return value
    }
    fun writeValue(value: Any?, file: File, fileConfiguration: FileConfiguration, path: String): Boolean {
        fileConfiguration[path] = value
        try {
            fileSaverCustomValues(fileConfiguration, file)
        } catch (exception: Exception) {
            Bukkit.getLogger().warning("Failed to write value for " + path + " in file " + file.name)
            return false
        }
        return true
    }

    fun removeValue(file: File, fileConfiguration: FileConfiguration, path: String) {
        writeValue(null, file, fileConfiguration, path)
    }
}