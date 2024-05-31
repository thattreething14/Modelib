package tree.modelib.blockbench

import org.bukkit.util.Vector
import tree.modelib.utils.Logger
import tree.modelib.utils.Round


class CubeBlueprint(projectResolution: Double, val cubeJSON: MutableMap<String, Any?>) {

    val to: Vector

    val from: Vector

    var validatedData: Boolean = false
    var boneOffset = Vector()
    init {
        //Sanitize data from ModelEngine which is not used by Minecraft resource packs
        cubeJSON.remove("rescale")
        cubeJSON.remove("locked")
        cubeJSON.remove("type")
        cubeJSON.remove("uuid")
        cubeJSON.remove("color")
        cubeJSON.remove("autouv")
        cubeJSON.remove("name")
        cubeJSON.remove("box_uv")
        cubeJSON.remove("render_order")
        cubeJSON.remove("allow_mirror_modeling")
        //process face textures
        processFace(projectResolution, cubeJSON["faces"] as Map<String, Any>?, "north")
        processFace(projectResolution, cubeJSON["faces"] as Map<String, Any>?, "east")
        processFace(projectResolution, cubeJSON["faces"] as Map<String, Any>?, "south")
        processFace(projectResolution, cubeJSON["faces"] as Map<String, Any>?, "west")
        processFace(projectResolution, cubeJSON["faces"] as Map<String, Any>?, "up")
        processFace(projectResolution, cubeJSON["faces"] as Map<String, Any>?, "down")

        //The model is scaled up 4x to reach the maximum theoretical size for large models, thus needs to be scaled correctly here
        //Note that how much it is scaled relies on the scaling of the head slot, it's somewhat arbitrary and just
        //works out that this is the right amount to get the right final size.
        val fromList = cubeJSON["from"] as? ArrayList<Double> ?: throw IllegalArgumentException("Invalid JSON data: 'from' field not found or incorrect type")
        from = Vector(
            Round.fourDecimalPlaces(fromList[0] * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER),
            Round.fourDecimalPlaces(fromList[1] * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER),
            Round.fourDecimalPlaces(fromList[2] * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER)
        )
        val toList = cubeJSON["to"] as? ArrayList<Double> ?: throw IllegalArgumentException("Invalid JSON data: 'from' field not found or incorrect type")
        to = Vector(
            Round.fourDecimalPlaces(toList[0] * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER),
            Round.fourDecimalPlaces(toList[1] * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER),
            Round.fourDecimalPlaces(toList[2] * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER)
        )
        validatedData = true
    }

    private fun processFace(projectResolution: Double, map: Map<String, Any>?, faceName: String) {
        setTextureData(projectResolution, map!![faceName] as MutableMap<String, Any?>?)
    }

    private fun setTextureData(projectResolution: Double, map: MutableMap<String, Any?>?) {
        if (map == null || map["texture"] == null) return
        val textureDouble = map["texture"] as Double?
        val textureValue = Math.round(textureDouble!!).toInt()
        map["texture"] = "#$textureValue"
        map["tintindex"] = 0
        map["rotation"] = 0
        val originalUV = map["uv"] as ArrayList<Double>?
        //For some reason Minecraft really wants images to be 16x16 so here we scale the UV to fit that
        val uvMultiplier = 16 / projectResolution
        map["uv"] = listOf(
            Round.fourDecimalPlaces(originalUV!![0] * uvMultiplier),
            Round.fourDecimalPlaces(originalUV[1] * uvMultiplier),
            Round.fourDecimalPlaces(originalUV[2] * uvMultiplier),
            Round.fourDecimalPlaces(originalUV[3] * uvMultiplier)
        )
    }

    fun shiftPosition() {
        from.subtract(boneOffset)
        to.subtract(boneOffset)
        cubeJSON["from"] = listOf(from.x, from.y, from.z)
        cubeJSON["to"] = listOf(to.x, to.y, to.z)
    }

    fun shiftRotation() {
        if (cubeJSON["origin"] == null) return
        val newRotationData: MutableMap<String, Any> = HashMap()

        val scaleFactor = 0.4

        //Adjust the origin
        val xOrigin: Double
        val yOrigin: Double
        val zOrigin: Double
        val originData: List<Double>? = cubeJSON["origin"] as ArrayList<Double>?
        xOrigin = originData!![0] * scaleFactor - boneOffset.x
        yOrigin = originData[1] * scaleFactor - boneOffset.y
        zOrigin = originData[2] * scaleFactor - boneOffset.z
        newRotationData["origin"] = listOf(xOrigin, yOrigin, zOrigin)

        var angle = 0.0
        var axis = "x"
        if (cubeJSON["rotation"] != null) {
            val rotations = cubeJSON["rotation"] as List<Double>?
            for (i in rotations!!.indices.reversed()) {
                if (rotations[i] != 0.0) {
                    angle = Round.oneDecimalPlace(rotations[i])
                    when (i) {
                        0 -> axis = "x"
                        1 -> axis = "y"
                        2 -> axis = "z"
                        else -> Logger.warn("Unexpected amount of rotation axes!")
                    }
                }
            }
        }

        newRotationData["angle"] = angle
        newRotationData["axis"] = axis
        cubeJSON["rotation"] = newRotationData
        cubeJSON.remove("origin")
    }
    fun getIsValidatedDataString(): String {
        return if (validatedData) "true" else "false"
    }

}