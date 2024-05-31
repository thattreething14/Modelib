package tree.modelib.model.animations

import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import tree.modelib.Plugin
import tree.modelib.blockbench.animations.AnimationsBlueprint
import tree.modelib.model.ModeledEntity
import tree.modelib.utils.LoopType
import kotlin.math.floor


class AnimationManager(private val modeledEntity: ModeledEntity, animationsBlueprint: AnimationsBlueprint?) {
    val animations = Animations(animationsBlueprint!!, modeledEntity)
    private val states = HashSet<Animation?>()

    //There are some animation defaults that will activate automatically so long as the animations are adequately named
    private var idleAnimation: Animation? = null
    private var attackAnimation: Animation? = null
    private var walkAnimation: Animation? = null
    private var jumpAnimation: Animation? = null
    private var deathAnimation: Animation? = null
    private var spawnAnimation: Animation? = null
    private var clock: BukkitTask? = null
    //This one is used for preventing default animations other than death from playing for as long as it is true
    private var animationGracePeriod = false

    init {
        idleAnimation = animations.animations["idle"]
        attackAnimation = animations.animations["attack"]
        walkAnimation = animations.animations["walk"]
        jumpAnimation = animations.animations["jump"]
        deathAnimation = animations.animations["death"]
        spawnAnimation = animations.animations["spawn"]
    }
    fun start() {
        if (spawnAnimation != null) {
            states.add(spawnAnimation)
            if (idleAnimation != null) {
                object : BukkitRunnable() {
                    override fun run() {
                        animationGracePeriod = false
                    }
                }.runTaskLater(Plugin.instance!!, spawnAnimation!!.animationBlueprint.duration * 1L)
            }
        } else if (idleAnimation != null) {
            states.add(idleAnimation)
        }

        clock = object : BukkitRunnable() {
            override fun run() {
                updateStates()

                // Create a copy of the states list to avoid ConcurrentModificationException
                val statesCopy = ArrayList(states)
                statesCopy.forEach { animation ->
                    playAnimationFrame(animation)
                }
                modeledEntity.skeleton?.transform()
            }
        }.runTaskTimer(Plugin.instance!!, 0, 1)
    }


    private fun updateStates() {
        if (modeledEntity.livingEntity == null) return
        if (modeledEntity.livingEntity!!.isDead) {
            if (deathAnimation != null) {
                animationGracePeriod = true
                overrideStates(deathAnimation)
                return
            } else {
                modeledEntity.remove()
                return
            }
        }
        if (animationGracePeriod) return
        if (jumpAnimation != null && !modeledEntity.livingEntity!!.isOnGround) {
            overrideStates(jumpAnimation)
            return
        }
        //val nearbyLivingEntities = modeledEntity.livingEntity?.getNearbyEntities(2.0, 2.0, 2.0)?.filterIsInstance<LivingEntity>()
        //if (attackAnimation != null && modeledEntity.livingEntity != null && nearbyLivingEntities?.isNotEmpty() == true) {
        //    overrideStates(attackAnimation)
        //    return
        //}


        if (walkAnimation != null && modeledEntity.livingEntity!!.velocity.length() > .08) {
            overrideStates(walkAnimation)
            return
        }
        overrideStates(idleAnimation)
        //Jump
        //if (!modeledEntity.getEntity().isDead())
    }

    private fun overrideStates(animation: Animation?) {
        if (animation != null && !states.contains(animation)) {
            states.clear()
            animation.resetCounter()
            states.add(animation)
        }
    }
    fun getDuration(animationName: String?): Long {
        val animation = animations.animations[animationName] ?: return -1
        return animation.animationBlueprint.duration.toLong()
    }


    private fun getAdjustedAnimationPosition(animation: Animation?): Int {
        animation ?: return 0 // Return default position if animation is null
        val adjustedAnimationPosition = if (animation.counter >= animation.animationBlueprint.duration && animation.animationBlueprint.loopType === LoopType.HOLD) {
            // Case where the animation is technically over but also is set to hold
            animation.animationBlueprint.duration - 1
        } else {
            // Normal case, looping
            (animation.counter - floor(animation.counter / animation.animationBlueprint.duration.toDouble()) * animation.animationBlueprint.duration).toInt()
        }
        return adjustedAnimationPosition
    }

    private fun playAnimationFrame(animation: Animation?) {
        animation ?: return // Return if animation is null

        if (animation.animationBlueprint.loopType != LoopType.LOOP && animation.counter >= animation.animationBlueprint.duration) {
            // Case where the animation doesn't loop, and it's over
            states.remove(animation)
            if (animation == deathAnimation) modeledEntity.remove()
            return
        }

        val adjustedAnimationPosition = getAdjustedAnimationPosition(animation)
        //Handle rotations
        animation.animationFrames.forEach { (key, value) ->
            value.getOrNull(adjustedAnimationPosition)?.let {
                key.updateAnimationRotation(
                    it.xRotation,
                    it.yRotation,
                    it.zRotation
                )
            }
        }

        //Handle translations
        animation.animationFrames.forEach { (key, value) ->
            value.getOrNull(adjustedAnimationPosition)?.let {
                key.updateAnimationTranslation(
                    it.xPosition,
                    it.yPosition,
                    it.zPosition
                )
            }
        }

        animation.incrementCounter()
    }


    fun stop() {
        states.clear()
        animationGracePeriod = false
    }

    fun hasAnimation(animationName: String?): Boolean {
        return animations.animations.containsKey(animationName)
    }

    fun end() {
        if (clock != null) clock!!.cancel()
    }

    companion object {
        val loadedAnimations: List<AnimationManager>? = null
        private val unloadedAnimations: List<AnimationManager>? = null
        private fun getAdjustedAnimationPosition(animation: Animation?): Int {
            val adjustedAnimationPosition = if (animation!!.counter >= animation.animationBlueprint
                    .duration && animation.animationBlueprint.loopType === LoopType.HOLD
            ) //Case where the animation is technically over but also is set to hold
                animation.animationBlueprint.duration - 1
            else {
                //Normal case, looping
                (animation.counter - floor(
                    animation.counter / animation.animationBlueprint.duration.toDouble()
                ) * animation.animationBlueprint.duration)
            }
            return adjustedAnimationPosition as Int
        }
    }
    fun getAnimationList(): List<String> {
        return animations.animations.keys.toList()
    }
    fun playAnimation(animationName: String?, blendAnimation: Boolean): Boolean {
        val animation = animations.animations[animationName] ?: return false
        if (!blendAnimation) {
            states.clear()
            animationGracePeriod = true
            object : BukkitRunnable() {
                override fun run() {
                    animationGracePeriod = false
                }
            }.runTaskLater(Plugin.instance!!, animation.animationBlueprint.duration  * 1L )
        }
        animation.resetCounter()
        states.add(animation)
        return true
    }

}