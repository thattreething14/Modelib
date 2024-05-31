package tree.modelib.blockbench.animations

import org.apache.logging.log4j.core.tools.picocli.CommandLine.InitializationException
import tree.modelib.blockbench.BoneBlueprint
import tree.modelib.blockbench.SkeletonBlueprint
import tree.modelib.utils.Logger
import tree.modelib.utils.LoopType
import tree.modelib.utils.TransformationType


class AnimationBlueprint(data: Any?, modelName: String, skeletonBlueprint: SkeletonBlueprint) {
    val boneKeyframes: HashMap<BoneBlueprint, List<Keyframe>> = hashMapOf()
    val animationFrames: HashMap<BoneBlueprint, Array<AnimationFrame>> = hashMapOf()
    var loopType: LoopType? = null
    var animationName: String? = null
    private val skeletonBlueprint: SkeletonBlueprint
    var duration: Int = 0

    init {
        val animationData: Map<String, Any>?
        try {
            animationData = data as Map<String, Any>?
        } catch (e: Exception) {
            Logger.warn("Failed to get animation data! Model format is not as expected, this version of BlockBench is not compatible!")
            e.printStackTrace()
            throw InitializationException("Failed to initialize AnimationBlueprint")
        }

        this.skeletonBlueprint = skeletonBlueprint
        initializeGlobalValues(animationData)

        if (animationData?.get("animators") == null) {
            throw InitializationException("No animators found")
        }
        (animationData?.get("animators") as Map<String, Any>).entries.forEach { pair -> initializeBones(pair.value as Map<String, Any>, modelName, animationName!!) }

        try {
            interpolateKeyframes()
        } catch (e: Exception) {
            Logger.warn("Failed to interpolate animations for model $modelName! Animation name: $animationName")
            e.printStackTrace()
        }
    }

    fun lerp(start: Double, end: Double, t: Double): Double {
        return (1 - t) * start + t * end
    }

    private fun initializeGlobalValues(animationData: Map<String, Any>?) {
        animationName = animationData?.get("name") as String?
        loopType = LoopType.valueOf((animationData?.get("loop") as String).toUpperCase())
        duration = (20 * (animationData["length"] as Double)).toInt()
    }

    private fun initializeBones(animationData: Map<String, Any>, modelName: String, animationName: String) {
        val boneName = animationData["name"] as String
        val boneBlueprint = skeletonBlueprint.boneMap[boneName]
        if (boneName.equals("hitbox", ignoreCase = true)) return
        if (boneBlueprint == null) {
            Logger.warn("Failed to get bone $boneName from model $modelName!")
            return
        }
        val keyframes = mutableListOf<Keyframe>()
        for (keyframeData in (animationData["keyframes"] as List<*>)) {
            keyframeData?.let { Keyframe(it, modelName, animationName) }?.let { keyframes.add(it) }
        }
        keyframes.sortBy { it.timeInTicks }
        boneKeyframes[boneBlueprint] = keyframes
    }

    private fun interpolateKeyframes() {
        boneKeyframes.forEach { boneBlueprint, keyframes -> interpolateBoneKeyframes(boneBlueprint, keyframes) }
    }

    private fun interpolateBoneKeyframes(boneBlueprint: BoneBlueprint, keyframes: List<Keyframe>) {
        val rotationKeyframes = mutableListOf<Keyframe>()
        val positionKeyframes = mutableListOf<Keyframe>()
        val scaleKeyframes = mutableListOf<Keyframe>()
        keyframes.forEach { keyframe ->
            when (keyframe.transformationType) {
                TransformationType.ROTATION -> rotationKeyframes.add(keyframe)
                TransformationType.POSITION -> positionKeyframes.add(keyframe)
                TransformationType.SCALE -> scaleKeyframes.add(keyframe)
            }
        }

        val animationFramesArray = Array(duration) { AnimationFrame() }

        interpolateRotations(animationFramesArray, rotationKeyframes)
        interpolateTranslations(animationFramesArray, positionKeyframes)

        animationFrames[boneBlueprint] = animationFramesArray
    }

    private fun interpolateRotations(animationFramesArray: Array<AnimationFrame>, rotationKeyframes: List<Keyframe>) {
        var firstFrame: Keyframe? = null
        var previousFrame: Keyframe? = null
        var lastFrame: Keyframe? = null

        rotationKeyframes.forEachIndexed { index, animationFrame ->
            if (index == 0) {
                firstFrame = animationFrame
                previousFrame = animationFrame
                lastFrame = animationFrame
                return@forEachIndexed
            }

            val durationBetweenKeyframes = minOf(animationFrame.timeInTicks, duration) - previousFrame!!.timeInTicks
            (0 until durationBetweenKeyframes).forEach { j ->
                val currentFrame = j + previousFrame!!.timeInTicks
                animationFramesArray[currentFrame].xRotation = lerp(previousFrame!!.dataX, animationFrame.dataX, j / durationBetweenKeyframes.toDouble())
                animationFramesArray[currentFrame].yRotation = -lerp(previousFrame!!.dataY, animationFrame.dataY, j / durationBetweenKeyframes.toDouble())
                animationFramesArray[currentFrame].zRotation = lerp(previousFrame!!.dataZ, animationFrame.dataZ, j / durationBetweenKeyframes.toDouble())
            }
            previousFrame = animationFrame
            if (animationFrame.timeInTicks > lastFrame!!.timeInTicks) lastFrame = animationFrame
            if (animationFrame.timeInTicks < firstFrame!!.timeInTicks) firstFrame = animationFrame
        }

        if (lastFrame != null && lastFrame!!.timeInTicks < duration - 1) {
            val durationBetweenKeyframes = duration - lastFrame!!.timeInTicks
            (0 until durationBetweenKeyframes).forEach { j ->
                val currentFrame = j + previousFrame!!.timeInTicks
                animationFramesArray[currentFrame].xRotation = lastFrame!!.dataX
                animationFramesArray[currentFrame].yRotation = -lastFrame!!.dataY
                animationFramesArray[currentFrame].zRotation = lastFrame!!.dataZ
            }
        }

        if (firstFrame != null && firstFrame!!.timeInTicks > 0) {
            val durationBetweenKeyframes = firstFrame!!.timeInTicks
            val clampedDuration = minOf(durationBetweenKeyframes, duration - 1)
            (0 until clampedDuration).forEach { j ->
                animationFramesArray[j].xRotation = firstFrame!!.dataX
                animationFramesArray[j].yRotation = -firstFrame!!.dataY
                animationFramesArray[j].zRotation = firstFrame!!.dataZ
            }
        }
    }

    private fun interpolateTranslations(animationFramesArray: Array<AnimationFrame>, positionKeyframes: List<Keyframe>) {
        var firstFrame: Keyframe? = null
        var previousFrame: Keyframe? = null
        var lastFrame: Keyframe? = null

        positionKeyframes.forEachIndexed { index, animationFrame ->
            if (index == 0) {
                firstFrame = animationFrame
                previousFrame = animationFrame
                lastFrame = animationFrame
                return@forEachIndexed
            }

            val durationBetweenKeyframes = animationFrame.timeInTicks - previousFrame!!.timeInTicks
            (0 until durationBetweenKeyframes).forEach { j ->
                val currentFrame = j + previousFrame!!.timeInTicks
                animationFramesArray[currentFrame].xPosition = -lerp(previousFrame!!.dataX, animationFrame.dataX, j / durationBetweenKeyframes.toDouble()) / 16.0
                animationFramesArray[currentFrame].yPosition = lerp(previousFrame!!.dataY, animationFrame.dataY, j / durationBetweenKeyframes.toDouble()) / 16.0
                animationFramesArray[currentFrame].zPosition = lerp(previousFrame!!.dataZ, animationFrame.dataZ, j / durationBetweenKeyframes.toDouble()) / 16.0
            }
            previousFrame = animationFrame
            if (animationFrame.timeInTicks > lastFrame!!.timeInTicks) lastFrame = animationFrame
            if (animationFrame.timeInTicks < firstFrame!!.timeInTicks) firstFrame = animationFrame
        }

        if (lastFrame != null && lastFrame!!.timeInTicks < duration - 1) {
            val durationBetweenKeyframes = duration - lastFrame!!.timeInTicks
            (0 until durationBetweenKeyframes).forEach { j ->
                val currentFrame = j + previousFrame!!.timeInTicks
                animationFramesArray[currentFrame].xPosition = -lastFrame!!.dataX / 16.0
                animationFramesArray[currentFrame].yPosition = lastFrame!!.dataY / 16.0
                animationFramesArray[currentFrame].zPosition = lastFrame!!.dataZ / 16.0
            }
        }

        if (firstFrame != null && firstFrame!!.timeInTicks > 0) {
            val durationBetweenKeyframes = firstFrame!!.timeInTicks
            val clampedDuration = minOf(durationBetweenKeyframes, duration - 1)
            (0 until clampedDuration).forEach { j ->
                animationFramesArray[j].xPosition = -firstFrame!!.dataX / 16.0
                animationFramesArray[j].yPosition = firstFrame!!.dataY / 16.0
                animationFramesArray[j].zPosition = firstFrame!!.dataZ / 16.0
            }
        }
    }

    private fun interpolateScales(animationFramesArray: Array<AnimationFrame>, scaleKeyframes: List<Keyframe>) {
        var previousFrame: Keyframe? = null
        scaleKeyframes.forEachIndexed { _, animationFrame ->
            if (previousFrame == null) {
                previousFrame = animationFrame
                return@forEachIndexed
            }

            val durationBetweenKeyframes = animationFrame.timeInTicks - previousFrame!!.timeInTicks
            (0 until durationBetweenKeyframes).forEach { j ->
                val currentFrame = j + previousFrame!!.timeInTicks
                animationFramesArray[currentFrame].xScale = lerp(previousFrame!!.dataX, animationFrame.dataX, j / durationBetweenKeyframes.toDouble())
                animationFramesArray[currentFrame].yScale = lerp(previousFrame!!.dataY, animationFrame.dataY, j / durationBetweenKeyframes.toDouble())
                animationFramesArray[currentFrame].zScale = lerp(previousFrame!!.dataZ, animationFrame.dataZ, j / durationBetweenKeyframes.toDouble())
            }
            previousFrame = animationFrame
        }
    }
}