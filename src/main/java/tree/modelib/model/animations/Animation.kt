package tree.modelib.model.animations

import tree.modelib.blockbench.animations.AnimationBlueprint
import tree.modelib.blockbench.animations.AnimationFrame
import tree.modelib.model.ModeledEntity
import tree.modelib.model.bones.Bone

class Animation(val animationBlueprint: AnimationBlueprint, modeledEntity: ModeledEntity) {
    val animationFrames = HashMap<Bone, Array<AnimationFrame?>>()

    var counter = 0

    fun incrementCounter() {
        counter++
    }

    init {
        animationBlueprint.animationFrames.mapValues { (key, value) ->
            value.map { it } // Cast each non-null AnimationFrame to AnimationFrame? to match the expected type
        }.forEach { (key, value) ->
            for (bone in modeledEntity.skeleton?.getBones()!!) {
                if (bone.boneBlueprint == key) {
                    animationFrames[bone] = value.toTypedArray()
                    break
                }
            }
        }
    }


    fun resetCounter() {
        counter = 0
    }
}