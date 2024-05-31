/**
 * Once again highly suggest you check out https://github.com/MagmaGuy/FreeMinecraftModels/tree/master
 * as alot of this was downright copied from Magmaguy.
 */
package tree.modelib

import com.magmaguy.easyminecraftgoals.NMSManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import tree.modelib.api.ModelLibrary
import tree.modelib.commands.MainCommand
import tree.modelib.commands.ModelCommand
import tree.modelib.examplemob.IceGolemSpawnCommand
import tree.modelib.model.LegacyHitDetection
import tree.modelib.model.ModeledEntityEvents
import tree.modelib.model.type.AnimationEntity
import tree.modelib.model.type.DynamicEntity
import tree.modelib.model.type.StaticEntity
import tree.modelib.utils.Config

class ModelibPlugin : JavaPlugin() {
    override fun onEnable() {
        Plugin.instance = this
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        Bukkit.getConsoleSender().sendMessage("${ChatColor.BLUE}===============================")
        Bukkit.getConsoleSender().sendMessage("${ChatColor.BLUE}Enabling Modelib ${ChatColor.GOLD}${description.version}${ChatColor.BLUE}...")
        Bukkit.getConsoleSender().sendMessage("${ChatColor.BLUE}Author: ${ChatColor.GOLD}thattreething14 ${ChatColor.BLUE}(Discord: ${ChatColor.GOLD}Big_mop${ChatColor.BLUE})")
        Bukkit.getConsoleSender().sendMessage("${ChatColor.BLUE}===============================")
        Config.initializeConfig()
        ModelsFolder.initializeConfig()
        OutputFolder.initializeConfig()
        NMSManager.initializeAdapter(this)
        server.pluginManager.registerEvents(ModeledEntityEvents(), this)
        server.pluginManager.registerEvents(LegacyHitDetection(), this)
        getCommand("modelib")?.setExecutor(MainCommand)
        getCommand("modelib")?.tabCompleter = ModelCommand
        getCommand("igs")?.setExecutor(IceGolemSpawnCommand())
        val modelList = ModelLibrary.allModels().keys.toList().joinToString(", ")
        Bukkit.getConsoleSender().sendMessage("${ChatColor.GOLD}[Modelib] ${ChatColor.BLUE}The following models have been registered: $modelList")
    }
    override fun onDisable() {
        StaticEntity.shutdown()
        DynamicEntity.shutdown()
        AnimationEntity.shutdown()
        LegacyHitDetection.shutdown()
        // Plugin shutdown logic
    }
}
