package tree.modelib.commands

import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tree.modelib.blockbench.FileModelConverter
import tree.modelib.model.type.StaticEntity
import tree.modelib.utils.Messages
import kotlin.math.ceil
import kotlin.math.sqrt

object GalleryCommand {
    fun spawnGallery(sender: CommandSender): Boolean {
        if (sender !is Player) {
            Messages.error(sender, "This command can only be executed by players.")
            return true
        }
        if (!sender.hasPermission("modelib.gallery")) {
            Messages.error(sender, "You do not have permission for this command.")
            return true
        }

        val playerLocation = sender.location
        val totalModels = FileModelConverter.convertedFileModels.size
        val sideLength = ceil(sqrt(totalModels.toDouble())).toInt()
        val startX = playerLocation.blockX - (sideLength / 2) * 10
        val startZ = playerLocation.blockZ - (sideLength / 2) * 10

        var x = startX
        var z = startZ

        for ((index, modelName) in FileModelConverter.convertedFileModels.keys.withIndex()) {
            val location = Location(playerLocation.world, x.toDouble(), playerLocation.blockY.toDouble(), z.toDouble())

            val staticEntity = StaticEntity.create(modelName, location)
            if (staticEntity == null) {
                Messages.error(sender, "Failed to load model: $modelName")
            }

            // Move to the next position
            x += 10
            if ((index + 1) % sideLength == 0) {
                x = startX
                z += 10
            }
        }

        Messages.print(sender, "Model gallery created.")
        return true
    }
}