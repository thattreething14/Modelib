package tree.modelib.model.bones

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import tree.modelib.Plugin
import tree.modelib.blockbench.BoneBlueprint
import tree.modelib.blockbench.SkeletonBlueprint

class Skeleton(skeletonBlueprint: SkeletonBlueprint) {
    val mainModel: MutableList<BoneBlueprint> = ArrayList()
    val boneMap: HashMap<String, Bone> = HashMap()
    var currentLocation: Location? = null
    private var hitbox: BoneBlueprint? = null
    private var damageTintTask: BukkitTask? = null
    val skeletonWatchers: SkeletonWatchers

    init {
        skeletonBlueprint.boneMap.forEach { (key, value) ->
            if (value.parent == null) {
                val bone = Bone(value, null, this)
                boneMap[key] = bone
                bone.getAllChildren(boneMap)
            }
        }
        skeletonWatchers = SkeletonWatchers(this)
    }

    fun generateDisplays(location: Location) {
        currentLocation = location
        boneMap.values.forEach { bone ->
            if (bone.parent == null) {
                bone.generateDisplay()
            }
        }
    }

    fun remove() {
        skeletonWatchers.remove()
        boneMap.values.forEach(Bone::remove)
    }

    fun setName(name: String?) {
        boneMap.values.forEach { bone -> bone.setName(name.toString()) }
    }

    fun setNameVisible(visible: Boolean) {
        boneMap.values.forEach { bone -> bone.setNameVisible(visible) }
    }

    fun getNametags(): List<ArmorStand> {
        val nametags: MutableList<ArmorStand> = ArrayList()
        boneMap.values.forEach { bone -> bone.getNametags(nametags) }
        return nametags
    }

    fun getBones(): Collection<Bone> {
        return boneMap.values
    }

    fun transform() {
        boneMap.values.forEach { bone ->
            if (bone.boneBlueprint.parent == null) {
                bone.transform()
            }
        }
    }

    fun tint() {
        damageTintTask?.cancel()
        damageTintTask = object : BukkitRunnable() {
            var counter = 0
            override fun run() {
                counter++
                if (counter > 10) {
                    cancel()
                    boneMap.values.forEach { bone -> bone.setHorseLeatherArmorColor(Color.WHITE) }
                    return
                }
                boneMap.values.forEach { bone ->
                    bone.setHorseLeatherArmorColor(Color.fromRGB(
                        255,
                        (255 / counter.toDouble()).toInt(),
                        (255 / counter.toDouble()).toInt()
                    ))
                }
            }
        }.runTaskTimer(Plugin.instance!!, 0, 1)
    }
}
