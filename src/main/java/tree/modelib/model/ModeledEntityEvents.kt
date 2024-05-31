package tree.modelib.model

import com.google.common.collect.ArrayListMultimap
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import tree.modelib.utils.ChunkHasher
import java.util.function.Consumer

class ModeledEntityEvents : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun ChunkLoadEvent(event: ChunkLoadEvent) {
        val chunkHash: Int = ChunkHasher.hash(event.chunk)
        val modeledEntities = unloadedModeledEntities[chunkHash] ?: return
        unloadedModeledEntities.removeAll(chunkHash)
        modeledEntities.forEach(Consumer { obj: ModeledEntity -> obj.loadChunk() })
        loadedModeledEntities.putAll(chunkHash, modeledEntities)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun ChunkUnloadEvent(event: ChunkUnloadEvent) {
        val chunkHash: Int = ChunkHasher.hash(event.chunk)
        val iterator = loadedModeledEntities.get(chunkHash).iterator()
        while (iterator.hasNext()) {
            val modeledEntity = iterator.next()
            if (modeledEntity.chunkHash != null && chunkHash == modeledEntity.chunkHash) {
                modeledEntity.unloadChunk()
                iterator.remove() // Remove the modeledEntity from the collection using the iterator
                unloadedModeledEntities.put(chunkHash, modeledEntity)
            }
        }
    }


    companion object {
        private val loadedModeledEntities: ArrayListMultimap<Int, ModeledEntity> = ArrayListMultimap.create()
        private val unloadedModeledEntities: ArrayListMultimap<Int, ModeledEntity> = ArrayListMultimap.create()

        fun addLoadedModeledEntity(modeledEntity: ModeledEntity) {
            loadedModeledEntities.put(modeledEntity.getChunkHash(), modeledEntity)
        }

        fun addUnloadedModeledEntity(modeledEntity: ModeledEntity) {
            unloadedModeledEntities.put(modeledEntity.getChunkHash(), modeledEntity)
        }

        fun removeLoadedModeledEntity(modeledEntity: ModeledEntity) {
            loadedModeledEntities.remove(modeledEntity.getChunkHash(), modeledEntity)
        }

        fun removeUnloadedModeledEntity(modeledEntity: ModeledEntity) {
            unloadedModeledEntities.remove(modeledEntity.getChunkHash(), modeledEntity)
        }
    }
}