package tree.modelib.model

import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import tree.modelib.Plugin


object RegisterModelEntity {
    private val ARMOR_STAND_KEY: NamespacedKey = NamespacedKey(Plugin.instance!!, "armor_stand")
    private val ENTITY_KEY: NamespacedKey = NamespacedKey(Plugin.instance!!, "entity")

    fun registerModelArmorStand(armorStand: ArmorStand, name: String) {
        armorStand.persistentDataContainer.set(ENTITY_KEY, PersistentDataType.STRING, name)
    }

    fun registerModelEntity(entity: Entity, name: String?) {
        entity.persistentDataContainer.set(ENTITY_KEY, PersistentDataType.STRING, name.toString())
    }

    fun isModelArmorStand(entity: Entity): Boolean {
        return entity.persistentDataContainer.keys.contains(ARMOR_STAND_KEY)
    }

    fun isModelEntity(entity: Entity): Boolean {
        return entity.persistentDataContainer.keys.contains(ENTITY_KEY)
    }
}