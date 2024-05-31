package tree.modelib.model.type

import com.magmaguy.easyminecraftgoals.NMSManager
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.BoundingBox
import tree.modelib.Plugin
import tree.modelib.blockbench.FileModelConverter
import tree.modelib.model.LegacyHitDetection
import tree.modelib.model.ModeledEntity
import tree.modelib.model.ModeledEntityInterface
import tree.modelib.model.RegisterModelEntity.registerModelEntity
import java.util.function.Consumer

class DynamicEntity(entityID: String?, targetLocation: Location?) :
    ModeledEntity(entityID!!, targetLocation!!), ModeledEntityInterface {
    private var skeletonSync: BukkitTask? = null
    init {
        dynamicEntities.add(this)
    }

    fun spawn(entity: LivingEntity?) {
        super.livingEntity = entity
        registerModelEntity(entity!!, skeletonBlueprint?.modelName)
        spawn()
        syncSkeletonWithEntity()
        if (skeletonBlueprint?.hitbox != null) NMSManager.getAdapter().setCustomHitbox(
            entity,
            skeletonBlueprint!!.hitbox!!.width.toFloat(),
            skeletonBlueprint!!.hitbox!!.height.toFloat(),
            true
        )
    }

    private fun syncSkeletonWithEntity() {
        skeletonSync = object : BukkitRunnable() {
            override fun run() {
                if (livingEntity == null || !livingEntity!!.isValid) {
                    remove()
                    cancel()
                    return
                }
                skeleton?.currentLocation = (livingEntity!!.location)
            }
        }.runTaskTimer(Plugin.instance!!, 0, 1)
    }

    override fun remove() {
        super.remove()
        livingEntity?.remove()
        if (skeletonSync != null) skeletonSync!!.cancel()
    }

    override fun getHitbox(): BoundingBox? {
        if (livingEntity == null) return null
        return livingEntity!!.boundingBox
    }

    override fun damage(player: Player?, damage: Double) {
        if (livingEntity == null) return
        LegacyHitDetection.entityDamageBypass = true
        livingEntity!!.damage(damage, player)
        skeleton?.tint()
    }

    override fun getWorld(): World? {
        if (livingEntity == null || !livingEntity!!.isValid) return null
        return livingEntity!!.world
    }

    companion object {
        val dynamicEntities: MutableList<DynamicEntity> = ArrayList()
        fun shutdown() {
            dynamicEntities.forEach(Consumer { obj: DynamicEntity -> obj.remove() })
            dynamicEntities.clear()
        }

        //safer since it can return null
        fun create(entityID: String?, livingEntity: LivingEntity): DynamicEntity? {
            FileModelConverter.convertedFileModels[entityID] ?: return null
            val dynamicEntity = DynamicEntity(entityID, livingEntity.location)
            dynamicEntity.spawn(livingEntity)
            livingEntity.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 0))

            return dynamicEntity
        }
    }
}