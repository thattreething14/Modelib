package tree.modelib.blockbench

import com.google.gson.Gson
import org.apache.commons.io.FileUtils
import tree.modelib.Plugin
import tree.modelib.blockbench.animations.AnimationsBlueprint
import java.io.ByteArrayInputStream
import java.io.File
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO

class FileModelConverter(file: File) {
    companion object {
        val convertedFileModels = HashMap<String, FileModelConverter>()
        val imageSize = HashMap<String, Int>()

    }

    private val values = HashMap<String, Any>()
    private val outliner = HashMap<String, Any>()
    private val textures = HashMap<Int, String>()
    private var modelName: String = ""
    val skeletonBlueprint: SkeletonBlueprint
    var animationsBlueprint: AnimationsBlueprint? = null
    val ID: String

    init {
            modelName = when {
                file.name.contains(".bbmodel") -> file.name.replace(".bbmodel", "")
                else -> {
                    println("File ${file.name} should not be in the models folder!")
                    throw IllegalArgumentException("Invalid file type")
                }
            }.toLowerCase()

        val gson = Gson()

        val reader: Reader
        try {
            reader = Files.newBufferedReader(Paths.get(file.path))
        } catch (ex: Exception) {
            println("Failed to read file ${file.absolutePath}")
            throw IllegalArgumentException("Invalid read")
        }

        val map: Map<*, *>
        map = try {
            gson.fromJson(reader, Map::class.java)
        } catch (ex: Exception) {
            println("Failed to parse JSON from file")
            throw IllegalArgumentException("Failed to parse JSON from file")
        }

        reader.close()

        val projectResolution = (map["resolution"] as Map<*, *>)["height"] as Double

        val texturesValues = map["textures"] as List<Map<*, *>>
        texturesValues.forEachIndexed { i, element ->
            var imageName = (element["name"] as String).toLowerCase()
            if (!imageName.contains(".png")) {
                imageName = if (!imageName.contains(".")) "$imageName.png" else "${imageName.split("\\.")[0]}.png"
            }
            val base64Image = (element["source"] as String).split(",")[1]
            val id = i
            textures[id] = imageName.replace(".png", "")
            if (!imageSize.containsKey(imageName)) {
                try {
                    val inputStream = ByteArrayInputStream(Base64.getDecoder().decode(base64Image))
                    val imageFile = File(Plugin.instance?.dataFolder!!.absolutePath + File.separatorChar + "output" + File.separatorChar + "Modelib" + File.separatorChar + "assets" + File.separatorChar + "modelib" + File.separatorChar + "textures" + File.separatorChar + "entity" + File.separatorChar + imageName)
                    FileUtils.writeByteArrayToFile(imageFile, inputStream.readAllBytes())
                    val bufferedImage = ImageIO.read(imageFile)
                    imageSize[imageName] = bufferedImage.width
                } catch (ex: Exception) {
                    println("Failed to convert image $imageName to its corresponding image file!")
                    return@forEachIndexed
                }
            }
        }

        val elementValues = map["elements"] as List<Map<*, *>>
        elementValues.forEach { element ->
            values[element["uuid"] as String] = element
        }

        val outlinerValues = map["outliner"] as List<*>
        outlinerValues.forEach { item ->
            if (item is Map<*, *>) {
                outliner[item["uuid"] as String] = item
            }
        }

        ID = modelName
        skeletonBlueprint = SkeletonBlueprint(
            projectResolution,
            outlinerValues as List<Any>,
            values,
            generateFileTextures(),
            modelName
        )



        val animationList = map["animations"] as? ArrayList<*>
        if (animationList != null) animationsBlueprint = AnimationsBlueprint(animationList, modelName, skeletonBlueprint)
            convertedFileModels[modelName] = this
            // renderData() debugging purposes
    }

    fun shutdown() {
        convertedFileModels.clear()
        imageSize.clear()
    }
//    fun renderData() {
//        Developer.info(animationsBlueprint?.animations?.toList().toString())
//    }
    private fun generateFileTextures(): Map<String, Map<String, Any>> {
        val texturesMap = HashMap<String, Map<String, Any>>()
        val textureContents = HashMap<String, Any>()
        textures.forEach { key, value ->
            textureContents["$key"] = "modelib:entity/$value"
        }
        texturesMap["textures"] = textureContents
        return texturesMap
    }
}