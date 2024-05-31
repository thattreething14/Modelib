package tree.modelib.model

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import tree.modelib.blockbench.BoneBlueprint
import tree.modelib.blockbench.FileModelConverter
import tree.modelib.blockbench.SkeletonBlueprint
import tree.modelib.model.animations.AnimationManager
import tree.modelib.model.bones.Skeleton
import tree.modelib.utils.ChunkHasher

open class ModeledEntity(entityID: String, private val spawnLocation: Location) : ModeledEntityInterface {
     val name: String = "default"
    var chunkHash: Int? = null
    var livingEntity: LivingEntity? = null
    private var animationManager: AnimationManager? = null
    var skeletonBlueprint: SkeletonBlueprint? = null
    var lastSeenLocation: Location = spawnLocation
    var skeleton: Skeleton? = null
    private val world: World? = spawnLocation.world

    init {
        ModeledEntityEvents.addLoadedModeledEntity(this)
        val fileModelConverter = FileModelConverter.convertedFileModels[entityID]
        if (fileModelConverter != null) {
            skeletonBlueprint = fileModelConverter.skeletonBlueprint
            skeleton = Skeleton(skeletonBlueprint!!)
            if (fileModelConverter.animationsBlueprint != null) {
                animationManager = AnimationManager(this, fileModelConverter.animationsBlueprint!!)
            }
        }
    }
    private fun isNameTag(armorStand: ArmorStand): Boolean {
        return armorStand.persistentDataContainer.has(BoneBlueprint.nameTagKey, PersistentDataType.BYTE)
    }
    fun getSpawnLocation(): Location {
        return spawnLocation.clone()
    }

    private fun armorStandInitializer(targetLocation: Location) {
        skeleton?.generateDisplays(targetLocation)
    }

    fun spawn(location: Location = lastSeenLocation) {
        armorStandInitializer(location)
        animationManager?.start()
    }
    fun getDuration(animationName: String): Long? {
        return animationManager?.getDuration(animationName)
    }
    fun playAnimation(animationName: String, blendAnimation: Boolean): Boolean {
        return animationManager?.playAnimation(animationName, blendAnimation) ?: false
    }

    fun loadChunk() {
        spawn()
    }

    open fun remove() {
        skeleton?.remove()
        livingEntity?.remove()
        ModeledEntityEvents.removeLoadedModeledEntity(this)
        ModeledEntityEvents.removeUnloadedModeledEntity(this)
        terminateAnimation()
    }

    private fun terminateAnimation() {
        animationManager?.end()
    }

    fun stopCurrentAnimations() {
        animationManager?.stop()
    }

    fun hasAnimation(animationName: String): Boolean {
        return animationManager?.hasAnimation(animationName) ?: false
    }
    fun getAnimationList(): List<String>? {
        return animationManager?.getAnimationList()
    }
    fun unloadChunk() {
        lastSeenLocation = location1
        skeleton?.remove()
        terminateAnimation()
    }

    fun setName(name: String) {
        skeleton?.setName(name)
    }

    fun setNameVisible(visible: Boolean) {
        skeleton?.setNameVisible(visible)
    }

    fun getNametagArmorstands(): List<ArmorStand> {
        return skeleton?.getNametags() ?: emptyList()
    }

    fun move(vector: Vector) {
    }

    val location1: Location
        get() = spawnLocation

    fun getChunkHash(): Int {
        return chunkHash ?: ChunkHasher.hash(location1)
    }

    override fun getWorld(): World? {

        //Overriden by extending classes
        return null
    }

    override fun damage(player: Player?, damage: Double) {

    }

    fun getLocation(): Location {
        return spawnLocation
    }

    override fun getHitbox(): BoundingBox? {
        //Overriden by extending classes
        return null
    }

    fun visualizeHitbox() {
        val boundingBox = getHitbox() ?: return
        val resolution = 4.0
        for (x in 0 until (boundingBox.widthX * resolution).toInt())
            for (y in 0 until (boundingBox.height * resolution).toInt())
                for (z in 0 until (boundingBox.widthX * resolution).toInt()) {
                    val newX = x / resolution + boundingBox.minX
                    val newY = y / resolution + boundingBox.minY
                    val newZ = z / resolution + boundingBox.minZ
                    val location = Location(world, newX, newY, newZ)
                    location.world?.spawnParticle(Particle.FLAME, location, 1, 0.0, 0.0, 0.0, 0.0)
                }
    }
}