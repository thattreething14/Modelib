package tree.modelib.model

import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

interface ModeledEntityInterface {
    fun getHitbox(): BoundingBox?
    fun getWorld(): World?
    fun damage(player: Player?, damage: Double)
}