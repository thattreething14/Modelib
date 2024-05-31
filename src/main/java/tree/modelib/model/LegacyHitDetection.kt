package tree.modelib.model

import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerAnimationType
import org.bukkit.scheduler.BukkitRunnable
import tree.modelib.Plugin
import tree.modelib.api.ModelLibrary
import tree.modelib.model.RegisterModelEntity.isModelArmorStand
import tree.modelib.model.RegisterModelEntity.isModelEntity
import java.util.*


//Used for versions prior to 1.19.4 which do not have advanced entity types that can be used for hitboxes
class LegacyHitDetection : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun playerAnimation(event: PlayerAnimationEvent) {
        if (cooldowns.contains(event.player.uniqueId)) return
        if (event.animationType != PlayerAnimationType.ARM_SWING) return
        addCooldown(event.player)
        val modeledEntity = raytraceForModeledEntity(event.player) ?: return
        event.isCancelled = true
        //todo: this requires maths
        damageCustomModelEntity(modeledEntity, event.player, 2.0)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun playerMeleeAttack(event: EntityDamageByEntityEvent) {
        if (cooldowns.contains(event.damager.uniqueId)) return
        if (!isModelEntity(event.entity) && !isModelArmorStand(event.entity)) return
        if (entityDamageBypass) {
            entityDamageBypass = false
            return
        }
        val player: Player = when (val damager = event.damager) {
            is Player -> damager
            is Projectile -> {
                val shooter = damager.shooter
                if (shooter is Player) {
                    shooter
                } else {
                    return
                }
            }

            else -> return
        }

        addCooldown(player)
        event.isCancelled = true
        val modeledEntity = raytraceForModeledEntity(player) ?: return
        damageCustomModelEntity(modeledEntity, player, event.damage)
    }


    companion object {
        private const val meleeRange = 3
        private const val resolution = 4
        private val cooldowns: HashSet<Any?> = HashSet<Any?>()

        var entityDamageBypass = false

        fun shutdown() {
            cooldowns.clear()
        }

        fun raytraceForModeledEntity(player: Player): ModeledEntity? {
            val startLocation = player.eyeLocation
            val ray = player.eyeLocation.direction.normalize().multiply(resolution / 10.0)
            val modeledEntities: MutableList<ModeledEntity> = ModelLibrary.allEntities as MutableList<ModeledEntity>
            var modeledEntity: ModeledEntity? = null
            modeledEntities.removeIf { thisEntity: ModeledEntity -> thisEntity.getWorld() == null || thisEntity.getWorld() != player.world }
            if (modeledEntities.isEmpty()) return modeledEntity

            for (i in 0 until meleeRange * resolution) {
                if (modeledEntity != null) break
                startLocation.add(ray)
                for (entity in modeledEntities) {
                    val boundingBox = entity.getHitbox() ?: continue
                    if (!boundingBox.contains(startLocation.toVector())) continue
                    modeledEntity = entity
                    break
                }
            }
            return modeledEntity
        }

        private fun damageCustomModelEntity(modeledEntity: ModeledEntity, player: Player, damage: Double) {
            modeledEntity.damage(player, damage)
        }

        private fun addCooldown(player: Player) {
            cooldowns.add(player.uniqueId)
            object : BukkitRunnable() {
                override fun run() {
                    cooldowns.remove(player.uniqueId)
                }
            }.runTaskLater(Plugin.instance!!, 1)
        }
    }
}