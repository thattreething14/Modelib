package tree.modelib.utils

import org.bukkit.Chunk
import org.bukkit.Location
import java.util.*


object ChunkHasher {
    fun hash(chunk: Chunk): Int {
        return Objects.hash(chunk.x, chunk.z, chunk.world.uid)
    }

    //pseudo-chunks - prevent it form having to load the chunk
    fun hash(x: Int, z: Int, worldUUID: UUID?): Int {
        return Objects.hash(x, z, worldUUID)
    }

    fun hash(location: Location): Int {
        return Objects.hash(location.blockX shr 4, location.blockZ shr 4, location.world!!.uid)
    }

    fun hash(x: Double, z: Double): Vector<Double> {
        val vector = Vector<Double>(2)
        vector.addElement(x)
        vector.addElement(z)
        return vector
    }

    fun isSameChunk(chunk: Chunk, hashedChunk: Int): Boolean {
        return hash(chunk) == hashedChunk
    }
}