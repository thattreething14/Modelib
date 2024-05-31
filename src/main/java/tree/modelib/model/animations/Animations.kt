package tree.modelib.model.animations

import tree.modelib.blockbench.animations.AnimationsBlueprint
import tree.modelib.model.ModeledEntity

class Animations(animationBlueprint: AnimationsBlueprint, modeledEntity: ModeledEntity) {
    val animations: HashMap<String, Animation> = HashMap()

    init {
        animationBlueprint.animations.forEach { (key, value) ->
            if (key != null) {
                animations[key] = Animation(value, modeledEntity)
            }
        }
    }
}
