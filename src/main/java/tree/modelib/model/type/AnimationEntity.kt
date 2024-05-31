package tree.modelib.model.type

import org.bukkit.Bukkit
import org.bukkit.Location
import tree.modelib.Plugin
import tree.modelib.blockbench.FileModelConverter
import tree.modelib.model.ModeledEntity
import tree.modelib.model.ModeledEntityInterface
import java.util.function.Consumer

class AnimationEntity(entityID: String, targetLocation: Location, private val animationName: String) :
    ModeledEntity(entityID, targetLocation), ModeledEntityInterface {

    private val despawnDelayTicks: Long? = if (hasAnimation(animationName)) getDuration(animationName) else null
    fun spawn() {
        super.spawn(lastSeenLocation)
        playAnimation(animationName, false)
        Bukkit.getScheduler().runTaskLater(Plugin.instance!!, Runnable {
            remove()
        }, despawnDelayTicks!!.toLong())
    }
    companion object {
       val animationEntities: MutableList<AnimationEntity> = ArrayList()
        fun shutdown() {
            animationEntities.forEach(Consumer { obj: AnimationEntity -> obj.remove() })
            animationEntities.clear()
        }

        //safer since it can return null
        fun create(entityID: String, targetLocation: Location, animationName: String): AnimationEntity? {
            FileModelConverter.convertedFileModels[entityID] ?: return null
            val animationEntity = AnimationEntity(entityID, targetLocation, animationName)
            animationEntity.spawn()
            return animationEntity
        }
    }
}