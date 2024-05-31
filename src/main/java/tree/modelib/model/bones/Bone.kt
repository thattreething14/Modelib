package tree.modelib.model.bones

import org.bukkit.Color
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import tree.modelib.blockbench.BoneBlueprint
import tree.modelib.utils.Config
import tree.modelib.utils.VersionChecker
import java.util.*

class Bone(
    val boneBlueprint: BoneBlueprint,
    val parent: Bone?,
    val skeleton: Skeleton
) {
    private val boneChildren: MutableList<Bone> = mutableListOf()
//    private val reset = 20 * 30
//    private var lastUpdate = System.currentTimeMillis()
    private val animationInterval = 1000 / Config.frameRate // milliseconds (adjust for desired frame rate)
    private var lastAnimationTime = System.currentTimeMillis()
    val boneTransforms: BoneTransforms = BoneTransforms(this, parent)
    var animationTranslation = Vector()
    var animationRotation = Vector()
    private var counter = 0

    init {
        parent?.boneChildren?.add(this)
        boneBlueprint.boneBlueprintChildren.forEach {
            boneChildren.add(Bone(it, this, skeleton))
        }
    }

    fun updateAnimationTranslation(x: Double, y: Double, z: Double) {
        animationTranslation = Vector(x, y, z)
    }

    fun updateAnimationRotation(x: Double, y: Double, z: Double) {
        animationRotation = Vector(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z))
    }

    fun transform() {
        boneTransforms.transform()
        boneChildren.forEach { it.transform() }
        skeleton.skeletonWatchers.addToUpdateQueue(this) // Add the bone to the update queue for batch packet sending
    }


    fun generateDisplay() {
        boneTransforms.generateDisplay()
        boneChildren.forEach { it.generateDisplay() }
    }

    fun setName(name: String) {
        boneChildren.forEach { it.setName(name) }
    }

    fun setNameVisible(visible: Boolean) {
        boneChildren.forEach { it.setNameVisible(visible) }
    }

    fun getNametags(nametags: MutableList<ArmorStand>) {
        boneChildren.forEach { it.getNametags(nametags) }
    }

    fun remove() {
        boneTransforms.packetArmorStandEntity?.remove()
        boneTransforms.packetDisplayEntity?.remove()
        boneChildren.forEach { it.remove() }
    }

    fun getAllChildren(children: HashMap<String, Bone>) {
        boneChildren.forEach {
            children[it.boneBlueprint.boneName] = it
            it.getAllChildren(children)
        }
    }
    fun sendUpdatePacket() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnimationTime < animationInterval) {
            return
        }
        lastAnimationTime = currentTime
        synchronized(skeleton.skeletonWatchers) {
            // Reset the bone positions for all viewers
            skeleton.skeletonWatchers.reset()

            // Send update packets for bone positions
            boneTransforms.packetArmorStandEntity?.sendLocationAndRotationPacket(
                boneTransforms.getArmorStandTargetLocation(),
                boneTransforms.getArmorStandEntityRotation()
            )
            boneTransforms.packetDisplayEntity?.sendLocationAndRotationPacket(
                boneTransforms.getDisplayEntityTargetLocation(),
                boneTransforms.getDisplayEntityRotation()
            )
        }
    }

    fun displayTo(player: Player) {
        if (boneTransforms.packetArmorStandEntity != null &&
            (!Config.useDisplayEntitiesWhenPossible || VersionChecker.serverVersionOlderThan(19, 4))
        ) {
            boneTransforms.packetArmorStandEntity!!.displayTo(player.uniqueId)
        } else {
            boneTransforms.packetDisplayEntity?.displayTo(player.uniqueId)
        }
    }

    fun hideFrom(playerUUID: UUID) {
        boneTransforms.packetArmorStandEntity?.hideFrom(playerUUID)
        boneTransforms.packetDisplayEntity?.hideFrom(playerUUID)
    }

    fun setHorseLeatherArmorColor(color: Color) {
        boneTransforms.packetArmorStandEntity?.setHorseLeatherArmorColor(color)
        boneTransforms.packetDisplayEntity?.setHorseLeatherArmorColor(color)
    }
}