package tree.modelib.blockbench.animations

import tree.modelib.blockbench.SkeletonBlueprint

class AnimationsBlueprint(rawAnimationData: List<Any>, modelName: String, skeletonBlueprint: SkeletonBlueprint) {
    val animations: HashMap<String, AnimationBlueprint> = hashMapOf()

    init {
        for (animation in rawAnimationData) {
            val animationBlueprintObject = AnimationBlueprint(animation, modelName, skeletonBlueprint)
            animationBlueprintObject.animationName?.let { name ->
                animations[name] = animationBlueprintObject
            }
        }
    }
}

