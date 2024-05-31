package tree.modelib.model.bones

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import tree.modelib.Plugin
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.pow

class SkeletonWatchers(private val skeleton: Skeleton) {
    private val viewers: MutableSet<UUID> = Collections.synchronizedSet(HashSet())
    private var tick: BukkitTask? = null
    private val updateQueue: Queue<Bone> = ConcurrentLinkedQueue()
    private var clearTask: BukkitTask? = null
    private val clearIntervalTicks: Long = 60L // Fixes overload on memory--probably never tested it though

    init {
        tick()
        startClearTask()
    }

    fun remove() {
        tick?.cancel()
        clearTask?.cancel()
    }

    private fun tick() {
        tick = object : BukkitRunnable() {
            override fun run() {
                updateWatcherList()
                sendBatchUpdates()
            }
        }.runTaskTimerAsynchronously(Plugin.instance!!, 0, 1)
    }

    private fun startClearTask() {
        clearTask = object : BukkitRunnable() {
            override fun run() {
                updateQueue.clear()
            }
        }.runTaskTimer(Plugin.instance!!, clearIntervalTicks, clearIntervalTicks)
    }

    private fun updateWatcherList() {
        val newPlayers: MutableList<UUID> = ArrayList()
        val currentLocation = skeleton.currentLocation ?: return
        val world = currentLocation.world ?: return
        for (player in world.players) {
            val playerLocation = player.location
            if (playerLocation.distanceSquared(currentLocation) <
                2 * (Bukkit.getSimulationDistance() * 16.0).pow(2.0)
            ) {
                newPlayers.add(player.uniqueId)
                if (!viewers.contains(player.uniqueId)) displayTo(player)
            }
        }
        // Remove players who are no longer within range
        viewers.retainAll(newPlayers.toSet())
    }

    private fun sendBatchUpdates() {
        try {
            if (updateQueue.isNotEmpty()) {
                val bonesToUpdate = updateQueue.toList()
                updateQueue.clear()
                synchronized(skeleton.skeletonWatchers) {
                    bonesToUpdate.forEach { bone ->
                        sendPackets(bone)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendPackets(bone: Bone) {
        if (viewers.isEmpty()) return
        bone.sendUpdatePacket()
    }

    private fun displayTo(player: Player) {
        viewers.add(player.uniqueId)
        skeleton.getBones().forEach { bone ->
            bone.displayTo(player)
            updateQueue.add(bone)
        }
    }

    fun reset() {
        viewers.forEach { viewer ->
            displayTo(Plugin.instance!!.server.getPlayer(viewer) ?: return@forEach)
        }
    }

    fun addToUpdateQueue(bone: Bone) {
        updateQueue.add(bone)
    }
}
