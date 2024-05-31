package tree.modelib.model.type

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import tree.modelib.blockbench.FileModelConverter
import tree.modelib.model.ModeledEntity
import tree.modelib.model.ModeledEntityInterface
import tree.modelib.utils.ChunkHasher.hash
import java.util.function.Consumer

class StaticEntity(entityID: String, targetLocation: Location) :
    ModeledEntity(entityID, targetLocation), ModeledEntityInterface {
    private val health = -1.0
    private var hitbox: BoundingBox? = null

    init {
        staticEntities.add(this)
        chunkHash = hash(targetLocation)
    }

    fun spawn() {
        super.spawn(lastSeenLocation)
        if (skeletonBlueprint!!.hitbox != null) {
            val halfWidth = skeletonBlueprint!!.hitbox!!.width / 2.0
            val height = skeletonBlueprint!!.hitbox!!.height
            val modelOffset = skeletonBlueprint!!.hitbox!!.getModelOffset()
            val hitboxLocation = getSpawnLocation().add(modelOffset)
            hitbox = BoundingBox(
                hitboxLocation.x - halfWidth, hitboxLocation.y, hitboxLocation.z - halfWidth,
                hitboxLocation.x + halfWidth, hitboxLocation.y + height, hitboxLocation.z + halfWidth
            )
        }
    }

    override fun damage(player: Player?, damage: Double) {
        //If the health is -1, then the entity is not meant to be damageable.
        if (health == -1.0) return
        else remove()
    }

    override fun getWorld(): World? {
        val spawnLocation = getSpawnLocation()
        return spawnLocation.world
    }

    companion object {
        val staticEntities: MutableList<StaticEntity> = ArrayList()
        fun shutdown() {
            staticEntities.forEach(Consumer { obj: StaticEntity -> obj.remove() })
            staticEntities.clear()
        }

        //safer since it can return null
        fun create(entityID: String, targetLocation: Location): StaticEntity? {
            FileModelConverter.convertedFileModels[entityID] ?: return null
            val staticEntity = StaticEntity(entityID, targetLocation)
            staticEntity.spawn()
            return staticEntity
        }
    }

}