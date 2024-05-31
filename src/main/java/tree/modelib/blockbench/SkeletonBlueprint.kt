package tree.modelib.blockbench

import java.util.ArrayList
import java.util.HashMap

class SkeletonBlueprint(
    private val projectResolution: Double,
    outlinerJSON: List<Any>,
    values: HashMap<String, Any>,
    textureReferences: Map<String, Map<String, Any>>,
    val modelName: String
) {
    val boneMap: HashMap<String, BoneBlueprint> = HashMap()
    val mainModel: MutableList<BoneBlueprint> = ArrayList()
    var hitbox: HitboxBlueprint? = null

    init {
        for (i in outlinerJSON.indices) {
            if (outlinerJSON[i] !is Map<*, *>) continue
            val bone = outlinerJSON[i] as Map<String, Any>
            if ((bone["name"] as String).equals("hitbox", ignoreCase = true)) {
                hitbox = HitboxBlueprint(bone, values, modelName, null)
            } else {
                val boneBlueprint = BoneBlueprint(projectResolution, bone, values, textureReferences, modelName, null, this)
                mainModel.add(boneBlueprint)
              //  boneBlueprint.getRenderData()
            }
        }
    }
}
