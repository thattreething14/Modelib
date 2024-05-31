package tree.modelib.blockbench

import com.google.gson.Gson
import org.apache.commons.io.FileUtils
import org.bukkit.NamespacedKey
import org.bukkit.util.Vector
import tree.modelib.Plugin
import tree.modelib.utils.Config
import tree.modelib.utils.Logger
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class BoneBlueprint(
    projectResolution: Double,
    boneJSON: Map<String, Any>,
    values: HashMap<String, Any>,
    textureReferences: Map<String, Map<String, Any>>,
    modelName: String,
    parent: BoneBlueprint?,
    skeletonBlueprint: SkeletonBlueprint
) {
    companion object {
        const val ARMOR_STAND_HEAD_SIZE_MULTIPLIER = 0.4
        private const val MODEL_SCALE = 4.0
        const val ARMOR_STAND_PIVOT_POINT_HEIGHT = 1.438
        val nameTagKey = NamespacedKey(Plugin.instance!!, "NameTag")

    }
    /*
    debugging usage only
    fun getRenderData(): Pair<Vector, Vector> {
        for (childBone in boneBlueprintChildren) {
            val (finalPivot, rotation) = childBone.getRenderData()
            println("Bone: $boneName")
            Developer.info("Final Pivot: ${getBlueprintModelPivot()}")
            Developer.info("Rotation: $rotation")
            Developer.info("Center: ${getModelCenter()}")
        }
        return Pair(Vector(), Vector())
    }*/

    val boneBlueprintChildren = ArrayList<BoneBlueprint>()
    val cubeBlueprintChildren = ArrayList<CubeBlueprint>()
    val boneName: String
    val originalModelName: String
    val originalBoneName: String
    var modelID: Int? = null
    fun setModelID(id: Int) {
        modelID = id
    }
    var nameTag = false
    var parent: BoneBlueprint? = null
    var isItDisplayModel = true
    var blueprintModelCenter = Vector()
    lateinit var blueprintModelPivotVector: Vector
    var blueprintOriginalBoneRotationVector = Vector()

    init {
        originalBoneName = boneJSON["name"] as String
        boneName = "modelib:${modelName.lowercase(Locale.getDefault())}/${originalBoneName.lowercase(Locale.getDefault())}"
        originalModelName = modelName
        this.parent = parent
        if (originalBoneName.startsWith("tag_")) nameTag = true
        if (originalBoneName.startsWith("b_") || originalBoneName.lowercase(Locale.getDefault()) == "hitbox") isItDisplayModel = false
        processChildren(boneJSON, modelName, projectResolution, values, textureReferences, skeletonBlueprint)
        adjustCubes()
        processBoneValues(boneJSON)
        val filename = originalBoneName.lowercase(Locale.getDefault()).replace(" ", "_")
        generateAndWriteCubes(filename, textureReferences, modelName)
        skeletonBlueprint.boneMap[originalBoneName] = this
    }

    fun getBlueprintOriginalBoneRotation(): Vector {
        return blueprintOriginalBoneRotationVector.clone()
    }

    fun isDisplayModel(): Boolean {
        return isItDisplayModel && modelID != null
    }

    fun getModelCenter(): Vector {
        return blueprintModelCenter.clone().multiply(Config.scale * 5 / 32.0)
    }

    fun getBlueprintModelPivot(): Vector {
        return blueprintModelPivotVector.clone()
    }

    private fun adjustCubes() {
        if (cubeBlueprintChildren.isEmpty()) return
        var lowestX: Double? = null
        var lowestY: Double? = null
        var lowestZ: Double? = null
        var highestX: Double? = null
        var highestY: Double? = null
        var highestZ: Double? = null
        for (cubeBlueprint in cubeBlueprintChildren) {
            if (lowestX == null || cubeBlueprint.from.x < lowestX) lowestX = cubeBlueprint.from.x
            if (lowestY == null || cubeBlueprint.from.y < lowestY) lowestY = cubeBlueprint.from.y
            if (lowestZ == null || cubeBlueprint.from.z < lowestZ) lowestZ = cubeBlueprint.from.z
            if (highestX == null || cubeBlueprint.to.x > highestX) highestX = cubeBlueprint.to.x
            if (highestY == null || cubeBlueprint.to.y > highestY) highestY = cubeBlueprint.to.y
            if (highestZ == null || cubeBlueprint.to.z > highestZ) highestZ = cubeBlueprint.to.z
        }
        val xSize = Math.abs(highestX!! - lowestX!!)
        val ySize = Math.abs(highestY!! - lowestY!!)
        val zSize = Math.abs(highestZ!! - lowestZ!!)
        if (xSize > 48 * MODEL_SCALE || ySize > 48 * MODEL_SCALE || zSize > 48 * MODEL_SCALE) {
            Logger.warn("Model $originalModelName has a boneBlueprint or set of cubes which exceeds the maximum size! Either make the cubes smaller, less far apart or split them up into multiple bones!")
        }
        val newLowestX = 8 - xSize / 2.0
        val newLowestY = 8 - ySize / 2.0
        val newLowestZ = 8 - zSize / 2.0
        val xOffset = lowestX - newLowestX
        val yOffset = lowestY - newLowestY
        val zOffset = lowestZ - newLowestZ
        val cubeOffset = Vector(xOffset, yOffset, zOffset )
        for (cubeBlueprint in cubeBlueprintChildren) {
            cubeBlueprint.boneOffset = cubeOffset
            cubeBlueprint.shiftPosition()
            cubeBlueprint.shiftRotation()
        }
        cubeOffset.add(Vector(8, 8, 8))
        blueprintModelCenter = cubeOffset.clone()
    }

    private fun processBoneValues(boneJSON: Map<String, Any>) {
        setOrigin(boneJSON)
        setBoneRotation(boneJSON)
    }

    private fun processChildren(
        boneJSON: Map<String, Any>,
        modelName: String,
        projectResolution: Double,
        values: HashMap<String, Any>,
        textureReferences: Map<String, Map<String, Any>>,
        skeletonBlueprint: SkeletonBlueprint
    ) {
        val childrenValues = boneJSON["children"] as ArrayList<*>
        for (obj in childrenValues) {
            if (obj is String) {
                val cubeBlueprint = CubeBlueprint(projectResolution, values[obj] as MutableMap<String, Any?>)
                if (cubeBlueprint.validatedData) cubeBlueprintChildren.add(cubeBlueprint)
                else Logger.warn("Model $modelName has an invalid configuration for its cubes!")
            } else {
                boneBlueprintChildren.add(
                    BoneBlueprint(
                        projectResolution,
                        obj as Map<String, Any>,
                        values,
                        textureReferences,
                        modelName,
                        this,
                        skeletonBlueprint
                    )
                )
            }
        }
    }

    private fun setBoneRotation(boneJSON: Map<*, *>) {
        val boneRotation = boneJSON["rotation"]
        if (boneRotation == null) return
        val rotations = boneRotation as List<Double>
        blueprintOriginalBoneRotationVector = Vector(Math.toRadians(rotations[0]), Math.toRadians(rotations[1]), Math.toRadians(rotations[2]))
    }

    private fun setOrigin(boneJSON: Map<String, Any>) {
        val obj = boneJSON["origin"]
        if (obj == null) return
        val origins = obj as List<Double>
        blueprintModelPivotVector = getModelCenter().clone().subtract(
            Vector(
                origins[0],
                origins[1],
                origins[2]
            ).multiply(1 / 16.0)
        )
    }

    private fun generateAndWriteCubes(
        filename: String,
        textureReferences: Map<String, Map<String, Any>>,
        modelName: String
    ) {
        if (filename.equals("hitbox", ignoreCase = true) || filename.equals(
                "tag_name",
                ignoreCase = true
            ) || cubeBlueprintChildren.isEmpty()
        ) return
        val textureReferencesClone: MutableMap<String, Any> = HashMap(textureReferences)
        setDisplay(textureReferencesClone)
        writeCubes(textureReferencesClone)
        writeFile(modelName, filename, textureReferencesClone)
    }
    private fun getModelDirectory(modelName: String): String {
        return Plugin.instance?.dataFolder!!.absolutePath + File.separatorChar + "output" + File.separatorChar + "Modelib" + File.separatorChar + "assets" + File.separatorChar + "modelib" + File.separatorChar + "models" + File.separatorChar + modelName
    }
    private fun writeCubes(textureReferencesClone: MutableMap<String, Any>) {
        val cubeJSONs: MutableList<Any> = ArrayList()

        //This generates the JSON for each individual cubes
        for (cubeBlueprint in cubeBlueprintChildren) cubeJSONs.add(cubeBlueprint.cubeJSON)

        textureReferencesClone["elements"] = cubeJSONs
    }


    private fun setDisplay(textureReferencesClone: MutableMap<String, Any>) {
        textureReferencesClone["display"] = java.util.Map.of(
            "head", java.util.Map.of(
                "translation", java.util.List.of(
                    0,
                    -6.4,
                    0
                ),
                "scale", java.util.List.of<Any>(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE)
            )
        )
    }

    private fun writeFile(modelName: String, filename: String, boneJSON: Map<String, Any>) {
        val modelDirectory = getModelDirectory(modelName)
        val gson = Gson()
        try {
            FileUtils.writeStringToFile(File(modelDirectory + File.separatorChar + "$filename.json"), gson.toJson(boneJSON), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Logger.warn("Failed to write boneBlueprint resource packs for boneBlueprint $filename!")
            throw RuntimeException(e)
        }
    }
}