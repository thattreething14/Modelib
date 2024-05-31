package tree.modelib.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult
import tree.modelib.blockbench.FileModelConverter
import tree.modelib.model.type.AnimationEntity
import tree.modelib.model.type.DynamicEntity
import tree.modelib.model.type.StaticEntity
import tree.modelib.utils.Messages
import java.util.*

object ModelCommand : TabCompleter {
    private var animationEntity: AnimationEntity? = null
    fun modelCommand(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender !is Player) {
            Messages.error(sender, "This command can only be executed by players.")
            return true
        }
        if (!sender.hasPermission("modelib.model")) {
            Messages.error(sender, "You do not have permission for this command.")
            return true
        }
        if (args.size !in 4..5) {
            Messages.error(sender, "Usage: /model <modelName> <entityType> <entityMode>")
            return true
        }

        val modelName = args[1].lowercase(Locale.getDefault())
        val entityType = try {
            EntityType.valueOf(args[2].uppercase(Locale.getDefault()))
        } catch (e: IllegalArgumentException) {
            Messages.error(sender, "Invalid entity type.")
            return true
        }

        val entityMode = args[3].lowercase(Locale.getDefault())
        val rayTraceResult: RayTraceResult = sender.rayTraceBlocks(300.0) ?: run {
            Messages.error(sender, "You must be looking at a block to summon the model!")
            return false
        }

        val location = rayTraceResult.hitBlock!!.location.add(0.5, 1.0, 0.5)
        location.pitch = 0f
        location.yaw = 180f

        try {
            when (entityMode) {
                "dynamic" -> {
                    val entity: LivingEntity = sender.world.spawnEntity(location, entityType) as? LivingEntity
                        ?: throw IllegalStateException("Failed to spawn entity.")
                    val dynamicEntity = DynamicEntity.create(modelName, entity)
                    if (dynamicEntity == null) {
                        Messages.error(sender, "Failed to load model.")
                        return true
                    }
                }

                "static" -> {
                    val staticEntity = StaticEntity.create(modelName, location)
                    if (staticEntity == null) {
                        Messages.error(sender, "Failed to load model.")
                        return true
                    }
                }

                "animation" -> {
                    if (args.size != 5) {
                        Messages.error(sender, "Usage: /modelib model <modelName> <entityType> animation <animationName>")
                        return true
                    }
                    val animationName = args[4]
                    // Check if the animation exists
                    animationEntity = AnimationEntity(modelName, sender.location, animationName)
                    if (!animationEntity!!.hasAnimation(animationName)) {
                        Messages.error(sender, "Animation '$animationName' does not exist.")
                        Messages.error(
                            sender, "List of available animations: ${
                                animationEntity!!.getAnimationList()
                                    ?.joinToString(", ")
                            }"
                        )
                        return true
                    }
                    val animationEntity = AnimationEntity.create(modelName, location, animationName)
                    if (animationEntity == null) {
                        Messages.error(sender, "Failed to load animation entity.")
                        return true
                    }
                }


                else -> {
                    Messages.error(sender, "Invalid entity mode. Please specify 'dynamic', 'static', or 'animation'.")
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Messages.error(sender, "An error occurred while executing the command.")
            return true
        }

        Messages.print(sender, "Model loaded at your location.")
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        if (!sender.hasPermission("modelib.model")) return null
        if (args.isEmpty()) return null
        return when (args.size) {
            1 -> listOf("model", "reload", "gallery")
            2 -> {
                if (args[0] == "model") {
                    FileModelConverter.convertedFileModels.keys.toList()
                } else {
                    null
                }
            }
            3 -> {
                if (args[0] == "model") {
                    EntityType.entries.filter { it.isAlive }.map { it.name.lowercase(Locale.getDefault()) }
                } else {
                    null
                }
            }
            4 -> {
                if (args[0] == "model") {
                    listOf("dynamic", "static", "animation")
                } else {
                    null
                }
            }
            5 -> {
                if (args[0] == "model" && args[3].equals("animation", true)) {
                    val animationNames = animationEntity?.getAnimationList()
                    animationNames?.filter { it.startsWith(args[4], true) }
                } else {
                    null
                }
            }
            else -> null
        }
    }
}