package tree.modelib.blockbench

import org.bukkit.util.Vector
import tree.modelib.utils.Logger
import tree.modelib.utils.Round
import java.util.HashMap
import kotlin.math.abs


class HitboxBlueprint(
    boneJSON: Map<String, Any?>,
    values: HashMap<String, Any>,
    modelName: String,
    parent: BoneBlueprint?
) {
    private val modelName: String

    private var modelOffset = Vector()
    private var parent: BoneBlueprint? = null

    var width = 0.0

    var height = 0.0

    init {
        this.parent = parent
        this.modelName = modelName
        run {
            val childrenValues = boneJSON["children"] as ArrayList<*>?
            if (childrenValues!!.size > 1) {
                Logger.warn("Model $modelName has more than one value defining a hitbox! Only the first cube will be used to define the hitbox.")
            }
            if (childrenValues.isEmpty()) {
                Logger.warn("Model $modelName has a hitbox bone but no hitbox cube! This means the hitbox won't be able to generate correctly!")
                return@run
            }
            if (childrenValues[0] is String) {
                parseCube(values[childrenValues[0]] as Map<String, Any>?)
            } else {
                Logger.warn("Model $modelName has an invalid hitbox! The hitbox bone should only have one cube in it defining its boundaries.")
            }
        }
    }

    fun getModelOffset(): Vector {
        return modelOffset.clone()
    }

    private fun parseCube(cubeJSON: Map<String, Any>?) {
        val scaleFactor = .16 / 2.5
        val fromList = cubeJSON!!["from"] as ArrayList<Double>?
        val from = Vector(
            Round.fourDecimalPlaces(fromList!![0] * scaleFactor), Round.fourDecimalPlaces(
                fromList[1] * scaleFactor
            ), Round.fourDecimalPlaces(fromList[2] * scaleFactor)
        )
        val toList = cubeJSON["to"] as ArrayList<Double>?
        val to = Vector(
            Round.fourDecimalPlaces(toList!![0] * scaleFactor), Round.fourDecimalPlaces(
                toList[1] * scaleFactor
            ), Round.fourDecimalPlaces(toList[2] * scaleFactor)
        )
        width = abs(to.x - from.x)
        if (abs(abs(to.z - from.z) - width) > .1) Logger.warn(
            "Model " + modelName + " has a different X and Z value for the hitbox! x=" + width + " / z=" + abs(
                to.z - from.z
            ) + " ! Only the X value will be used for the hitbox."
        )
        height = abs(from.y - to.y)
        modelOffset = Vector(0, 0, 0)
    }
}