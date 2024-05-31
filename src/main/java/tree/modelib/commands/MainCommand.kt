package tree.modelib.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import tree.modelib.utils.Messages
import java.util.*
object MainCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            Messages.error(sender, "Please specify a subcommand: 'model' or 'reload'!")
            return true
        }

        val subCommand = args[0].lowercase(Locale.getDefault())
        when (subCommand) {
            "reload" -> ReloadCommand.reloadCommand(sender)
            "model" -> ModelCommand.modelCommand(sender, args)
            "gallery" -> GalleryCommand.spawnGallery(sender)
            else -> {
                Messages.error(sender, "Incorrect usage, please use 'model' or 'reload' or 'gallery'!")
                return true
            }
        }
        return true
    }
}

