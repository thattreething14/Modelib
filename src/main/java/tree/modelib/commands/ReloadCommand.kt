package tree.modelib.commands

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import tree.modelib.Plugin
import tree.modelib.blockbench.FileModelConverter
import tree.modelib.utils.Messages


object ReloadCommand {
    private fun reload(sender: CommandSender) {
        Plugin.instance!!.onDisable()
        Plugin.instance!!.onEnable()
        Messages.print(sender, "Successfully reloaded the plugin, ${FileModelConverter.convertedFileModels.size} models have been imported: ${ChatColor.BOLD}${FileModelConverter.convertedFileModels.keys.joinToString(", ")}")
    }

    fun reloadCommand(sender: CommandSender): Boolean {
        if (sender.hasPermission("modelib.reload")) {
            reload(sender)
        } else {
            Messages.error(sender, "You do not have permission to use this command!")
        }
        return true
    }
}