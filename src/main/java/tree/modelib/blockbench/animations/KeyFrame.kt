package tree.modelib.blockbench.animations

import tree.modelib.utils.Logger
import tree.modelib.utils.InterpolationType
import tree.modelib.utils.TransformationType
import java.util.*


class Keyframe(`object`: Any, modelName: String, animationName: String) {

     val transformationType: TransformationType

    val timeInTicks: Int

     val interpolationType: InterpolationType

    val dataX: Double

    val dataY: Double

    val dataZ: Double

    init {
        val data = `object` as Map<String, Any>
        transformationType = TransformationType.valueOf((data["channel"] as String?)!!.uppercase(Locale.getDefault()))
        interpolationType =
            InterpolationType.valueOf((data["interpolation"] as String?)!!.uppercase(Locale.getDefault()))
        timeInTicks = (20 * data["time"] as Double).toInt()
        val dataPoints =
            (data["data_points"] as List<Map<String?, Any?>>?)!![0]

        dataX = tryParseDouble(dataPoints["x"]!!, modelName, animationName)
        dataY = tryParseDouble(dataPoints["y"]!!, modelName, animationName)
        dataZ = tryParseDouble(dataPoints["z"]!!, modelName, animationName)
    }

    private fun tryParseDouble(rawObject: Any, modelName: String, animationName: String): Double {
        val inputString = if (rawObject is String) rawObject else rawObject.toString()
        val cleanedString = inputString.replace("\\n".toRegex(), "")
        if (cleanedString.isEmpty()) return 0.0
        try {
            return cleanedString.toDouble()
        } catch (e: Exception) {
            Logger.warn("Failed to parse supposed number value $inputString in animation $animationName for model $modelName!")
            return 0.0
        }
    }

}