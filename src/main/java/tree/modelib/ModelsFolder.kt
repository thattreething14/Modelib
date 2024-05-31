package tree.modelib
import com.google.gson.Gson
import org.apache.commons.io.FileUtils
import tree.modelib.blockbench.BoneBlueprint
import tree.modelib.blockbench.FileModelConverter
import tree.modelib.utils.Logger
import tree.modelib.utils.DirectoryHelper
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

object ModelsFolder {
    private var counter = 1
    private var folderCounter = 50

    fun initializeConfig() {
        counter = 1
        folderCounter = 50
        val file = DirectoryHelper.directoryCreator("models")
        if (!file.exists()) {
            Logger.warn("Failed to create models directory!")
            return
        }

        if (!file.isDirectory) {
            Logger.warn("Directory models was not a directory!")
            return
        }

        val gson = Gson()
        val bbModelConverterList = ArrayList<FileModelConverter>()
        val leatherHorseArmor: MutableMap<String, Any> = HashMap()
        leatherHorseArmor["parent"] = "item/generated"
        leatherHorseArmor["textures"] = Collections.singletonMap("layer0", "minecraft:item/leather_horse_armor")

        processFolders(file, bbModelConverterList, leatherHorseArmor, true)
        leatherHorseArmor["data"] = counter - 1 + folderCounter * 1000

        try {
            FileUtils.writeStringToFile(
                    File("${Plugin.instance!!.dataFolder.absolutePath}${File.separator}output${File.separator}Modelib${File.separator}assets${File.separator}minecraft${File.separator}models${File.separator}item${File.separator}leather_horse_armor.json"),
                gson.toJson(leatherHorseArmor), StandardCharsets.UTF_8
            )
        } catch (e: IOException) {
            Logger.warn("Failed to generate the iron horse armor file!")
            throw RuntimeException(e)
        }
    }

    private fun processFiles(childFile: File, bbModelConverterList: MutableList<FileModelConverter>, leatherHorseArmor: MutableMap<String, Any>) {
        try {
            val bbModelConverter = FileModelConverter(childFile)
            bbModelConverterList.add(bbModelConverter)
            bbModelConverter.skeletonBlueprint.mainModel.filter { it.boneName != "hitbox" }.forEach { assignBoneModelID(leatherHorseArmor, it) }
        } catch (e: Exception) {
            Logger.warn("Failed to parse model ${childFile.name}! Warn the developer about this")
            e.printStackTrace()
        }
    }

    private fun processFolders(file: File, bbModelConverterList: MutableList<FileModelConverter>, leatherHorseArmor: MutableMap<String, Any>, firstLevel: Boolean) {
        if (!firstLevel) folderCounter++
        val modelFiles = file.listFiles()
        modelFiles?.sortedBy { it.name }?.forEach { childFile ->
            if (childFile.isFile) processFiles(childFile, bbModelConverterList, leatherHorseArmor)
            else processFolders(childFile, bbModelConverterList, leatherHorseArmor, false)
        }
    }

    private fun assignBoneModelID(ironHorseArmorFile: MutableMap<String, Any>, boneBlueprint: BoneBlueprint) {
        val entryMap = HashMap<String, Any>()
        entryMap["predicate"] = Collections.singletonMap("custom_model_data", counter + folderCounter * 1000)
        if (boneBlueprint.cubeBlueprintChildren.isNotEmpty()) {
            boneBlueprint.modelID = counter + folderCounter * 1000
            counter++
        }
        entryMap["model"] = boneBlueprint.boneName.lowercase(Locale.getDefault())
        ironHorseArmorFile.computeIfAbsent("overrides") { ArrayList<Map<String, Any>>() }
        val existingList = ironHorseArmorFile["overrides"] as MutableList<Map<String, Any>>
        existingList.add(entryMap)
        ironHorseArmorFile["overrides"] = existingList
        if (boneBlueprint.boneBlueprintChildren.isNotEmpty())
            boneBlueprint.boneBlueprintChildren.forEach { assignBoneModelID(ironHorseArmorFile, it) }
    }
}
