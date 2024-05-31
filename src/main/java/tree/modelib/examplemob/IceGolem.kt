package tree.modelib.examplemob

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.attribute.AttributeModifier.Operation
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import tree.modelib.Plugin
import tree.modelib.model.type.AnimationEntity
import tree.modelib.model.type.DynamicEntity
import tree.modelib.utils.Logger
import java.util.*

class IceGolem(location: Location) : Listener {
    private val entity: LivingEntity = (location.world.spawnEntity(location, EntityType.IRON_GOLEM) as LivingEntity)
    private val dynamicEntity = DynamicEntity.create("ice_golem", entity)
    private var attackCooldown = System.currentTimeMillis()

    init {
        dynamicEntity?.playAnimation("roar", false)
        val animations = dynamicEntity?.getAnimationList()
        animations?.forEach { animationName ->
            Logger.info(animationName)
        }
        Plugin.instance!!.server.pluginManager.registerEvents(this, Plugin.instance!!)
        entity.customName = "Ice Golem"
        entity.isCustomNameVisible = true
        entity.isInvulnerable = false
        val maxHealthModifier = AttributeModifier(UUID.randomUUID(), "Max Health Modifier", 100.0, Operation.ADD_NUMBER)
        val maxHealthAttribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)
        maxHealthAttribute?.addModifier(maxHealthModifier)
        entity.health = 200.0
    }
    @EventHandler
    fun onMelee(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        if (damager === entity) {
            if (System.currentTimeMillis() - attackCooldown < 3000) {
                event.isCancelled = true
                return
            }
            event.isCancelled = true
            val random = Random()
            val health = entity.health
            val randomAnimation: String = if (health < 100 || random.nextInt(20) == 0) {
                "stomp"
            } else {
                if (random.nextBoolean()) "attack_right" else "attack_left"
            }
            dynamicEntity?.playAnimation(randomAnimation, false)
            val target = event.entity
            val damagerLocation = damager.location
            val entityLocation = entity.location
            val distance = damagerLocation.distance(entityLocation)
// use for lazar shooting
//            if (target is Player) {
//                val jawLocationVector = dynamicEntity?.skeletonBlueprint?.boneMap?.get("h_jaw")?.getModelCenter()
//                if (jawLocationVector != null) {
//                    val world = target.world
//                    val jawLocation = Location(world, jawLocationVector.x + entityLocation.x, jawLocationVector.y  + entityLocation.y, jawLocationVector.z + entityLocation.z)
//                    Messages.print(target, "Jaw Location: $jawLocation")
//                }
//            }
            Bukkit.getScheduler().runTaskLater(Plugin.instance!!, Runnable {
                if (!target.isDead && distance < 5) {
                    if (randomAnimation == "stomp") {
                        val nearbyEntities = target.location.world!!.getNearbyEntities(target.location, 10.0, 10.0, 10.0)
                        nearbyEntities.forEach { nearbyEntity ->
                            if (nearbyEntity is LivingEntity && nearbyEntity !== entity) {
                                nearbyEntity.velocity = Vector(0, 12, 0)
                                nearbyEntity.damage(40.0)
                            }
                        }
                    } else {
                        if (target is Player) {
                            if (target.isBlocking) {
                                event.isCancelled = true
                                val mainHand: ItemStack = target.inventory.itemInMainHand
                                if (mainHand.type == Material.SHIELD) {
                                    consumeShield(target, mainHand)
                                }
                                val offHand: ItemStack = target.inventory.itemInOffHand
                                if (offHand.type == Material.SHIELD) {
                                    consumeShield(target, offHand)
                                }
                            } else {
                                val damage = 30.0
                                val knockbackStrength = 3.0
                                target.damage(damage)
                                val direction = target.location.subtract(damager.location).toVector().normalize()
                                val knockback = direction.multiply(knockbackStrength)
                                target.velocity = knockback
                            }
                        } else if (target is LivingEntity && target !== entity) {
                            val damage = 30.0
                            val knockbackStrength = 3.0
                            target.damage(damage)
                            val direction = target.location.subtract(damager.location).toVector().normalize()
                            val knockback = direction.multiply(knockbackStrength)
                            target.velocity = knockback
                        }
                    }
                }
            }, 22L)
            attackCooldown = System.currentTimeMillis()
        }
    }
    private fun consumeShield(player: Player, shield: ItemStack) {
        shield.amount -= 1
        if (shield.amount <= 0) {
            if (player.inventory.itemInMainHand.isSimilar(shield)) {
                player.inventory.setItemInMainHand(null)
            } else if (player.inventory.itemInOffHand.isSimilar(shield)) {
                player.inventory.setItemInOffHand(null)
            }
            player.updateInventory()
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        if (event.entity === entity) {
            dynamicEntity?.remove()
            val deathAnimation = AnimationEntity("ice_golem", entity.location, "death")
            deathAnimation.spawn()
            val drops: MutableList<ItemStack> = ArrayList()
            drops.add(ItemStack(Material.ICE, 2))
            event.drops.clear()
            event.drops.addAll(drops)
        }
    }
}
