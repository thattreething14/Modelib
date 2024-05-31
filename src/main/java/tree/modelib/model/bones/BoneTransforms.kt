package tree.modelib.model.bones
import com.magmaguy.easyminecraftgoals.NMSManager
import com.magmaguy.easyminecraftgoals.internal.PacketModelEntity
import org.bukkit.Location
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import tree.modelib.blockbench.BoneBlueprint
import tree.modelib.utils.Config
import tree.modelib.utils.TransformationMatrix
import tree.modelib.utils.VersionChecker

class BoneTransforms(private val bone: Bone, private val parent: Bone?) {

    private val localMatrix = TransformationMatrix()
    private val globalMatrix = TransformationMatrix()
    var packetArmorStandEntity: PacketModelEntity? = null
        private set
    var packetDisplayEntity: PacketModelEntity? = null
        private set

    fun transform() {
        updateLocalTransform()
        updateGlobalTransform()
    }

    private fun updateGlobalTransform() {
        if (parent != null) {
            TransformationMatrix.multiplyMatrices(parent.boneTransforms.globalMatrix, localMatrix, globalMatrix)
        } else {
            globalMatrix.copyFrom(localMatrix)
        }
    }

    private fun updateLocalTransform() {
        localMatrix.resetToIdentityMatrix()
        translateModelCenter()
        shiftPivotPoint()
        rotateDefaultBoneRotation()
        translateAnimation()
        rotateAnimation()
        shiftPivotPointBack()
        rotateByEntityYaw()
    }

    private fun translateModelCenter() {
        val modelCenter = bone.boneBlueprint.getModelCenter()
        localMatrix.translate(modelCenter)

        parent?.boneBlueprint?.getModelCenter()?.let { parentModelCenter ->
            localMatrix.translate(parentModelCenter.clone().multiply(-1))
        }
    }

    private fun shiftPivotPoint() {
        localMatrix.translate(bone.boneBlueprint.blueprintModelPivotVector.clone().multiply(-1))
    }

    private fun translateAnimation() {
        val translation = bone.animationTranslation
        localMatrix.translate(translation.x.toFloat(), translation.y.toFloat(), translation.z.toFloat())
    }

    private fun rotateAnimation() {
        val test = Vector(bone.animationRotation.x, bone.animationRotation.y, -bone.animationRotation.z)
        test.rotateAroundY(Math.PI)
        localMatrix.rotate(test.x.toFloat(), test.y.toFloat(), test.z.toFloat())
    }

    private fun rotateDefaultBoneRotation() {
        val rotation = bone.boneBlueprint.blueprintOriginalBoneRotationVector
        localMatrix.rotate(rotation.x.toFloat(), rotation.y.toFloat(), rotation.z.toFloat())
    }

    private fun shiftPivotPointBack() {
        localMatrix.translate(bone.boneBlueprint.blueprintModelPivotVector.clone())
    }

    fun generateDisplay() {
        transform()
        if (bone.boneBlueprint.isItDisplayModel) {
            initializeDisplayEntityBone()
            initializeArmorStandBone()
        }
    }

    private fun initializeDisplayEntityBone() {
        if (!Config.useDisplayEntitiesWhenPossible) return
        val modelID = bone.boneBlueprint.modelID ?: return
        val targetLocation = getDisplayEntityTargetLocation()
        val adapter = NMSManager.getAdapter() ?: return
        packetDisplayEntity = adapter.createPacketDisplayEntity(targetLocation)
        packetDisplayEntity?.initializeModel(targetLocation, modelID)
        packetDisplayEntity?.setScale(2.5F * Config.scale)
        packetDisplayEntity?.sendLocationAndRotationPacket(targetLocation, getDisplayEntityRotation())
    }

    private fun initializeArmorStandBone() {
        val adapter = NMSManager.getAdapter()
        if (adapter != null) {
            val modelID = bone.boneBlueprint.modelID
            val targetLocation = getArmorStandTargetLocation()
            if (modelID != null) {
                packetArmorStandEntity = adapter.createPacketArmorStandEntity(targetLocation)
                packetArmorStandEntity?.initializeModel(targetLocation, modelID)
                packetArmorStandEntity?.sendLocationAndRotationPacket(targetLocation, getArmorStandEntityRotation())
            }
        }
    }

    private fun rotateByEntityYaw() {
        if (parent == null) {
            localMatrix.rotate(0f, (-Math.toRadians((bone.skeleton.currentLocation!!.yaw + 180).toDouble())).toFloat(), 0f)
        }
    }

    fun getArmorStandTargetLocation(): Location {
        val translatedGlobalMatrix = globalMatrix.applyTransformation(floatArrayOf(0f, 0f, 0f, 1f))
        val armorStandLocation = Location(
            bone.skeleton.currentLocation!!.world,
            translatedGlobalMatrix[0].toDouble(),
            translatedGlobalMatrix[1].toDouble(),
            translatedGlobalMatrix[2].toDouble()
        )
        armorStandLocation.add(bone.skeleton.currentLocation!!)
        armorStandLocation.yaw = 180f
        armorStandLocation.subtract(Vector(0.0, BoneBlueprint.ARMOR_STAND_PIVOT_POINT_HEIGHT, 0.0))
        return armorStandLocation
    }


    fun getDisplayEntityTargetLocation(): Location {
        val translatedGlobalMatrix = globalMatrix.applyTransformation(floatArrayOf(0f, 0f, 0f, 1f))
        val armorStandLocation: Location
        if (!VersionChecker.serverVersionOlderThan(20, 0)) {
            armorStandLocation = Location(
                bone.skeleton.currentLocation!!.world,
                translatedGlobalMatrix[0].toDouble(),
                translatedGlobalMatrix[1].toDouble(),
                translatedGlobalMatrix[2].toDouble()
            )
                .add(bone.skeleton.currentLocation!!)
            armorStandLocation.yaw = 180f
        } else armorStandLocation = Location(
            bone.skeleton.currentLocation!!.world,
            translatedGlobalMatrix[0].toDouble(),
            translatedGlobalMatrix[1].toDouble(),
            translatedGlobalMatrix[2].toDouble()
        )
            .add(bone.skeleton.currentLocation!!)
        return armorStandLocation
    }

    fun getDisplayEntityRotation(): EulerAngle {
        val rotation = globalMatrix.rotation
        return if (VersionChecker.serverVersionOlderThan(20, 0)) {
            EulerAngle(rotation[0].toDouble(), rotation[1].toDouble(), rotation[2].toDouble())
        } else {
            EulerAngle(-rotation[0].toDouble(), rotation[1].toDouble(), -rotation[2].toDouble())
        }
    }

    fun getArmorStandEntityRotation(): EulerAngle {
        val rotation = globalMatrix.rotation
        return EulerAngle(-rotation[0].toDouble(), -rotation[1].toDouble(), rotation[2].toDouble())
    }
}
