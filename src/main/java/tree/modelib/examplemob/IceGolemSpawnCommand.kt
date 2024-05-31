package tree.modelib.examplemob

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tree.modelib.model.type.DynamicEntity
import tree.modelib.utils.Messages

class IceGolemSpawnCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            Messages.error(sender,"Only players can execute this command.")
            return true
        }
        if (!sender.hasPermission("modelib.igs")) {
            Messages.error(sender, "You do not have permission for this command.")
            return true
        }
        // Spawn IceGolem test mob at player location
        IceGolem(sender.location)
        Messages.print(sender, "Ice Golem spawned at your location!")
        return true
    }
}