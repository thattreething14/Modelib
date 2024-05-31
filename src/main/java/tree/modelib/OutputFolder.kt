package tree.modelib

import tree.modelib.utils.DirectoryHelper
import tree.modelib.utils.ZipFile
import java.io.File

object OutputFolder {
    fun initializeConfig() {
        DirectoryHelper.directoryCreator("output")
        DirectoryHelper.directoryCreator("output" + File.separatorChar + "Modelib")
        DirectoryHelper.directoryCreator("output" + File.separatorChar + "Modelib" + File.separatorChar + "assets")
        DirectoryHelper.directoryCreator("output" + File.separatorChar + "Modelib" + File.separatorChar + "assets" + File.separatorChar + "modelib")
        DirectoryHelper.directoryCreator("output" + File.separatorChar + "Modelib" + File.separatorChar + "assets" + File.separatorChar + "modelib" + File.separatorChar + "textures")
        DirectoryHelper.directoryCreator("output" + File.separatorChar + "Modelib" + File.separatorChar + "assets" + File.separatorChar + "modelib" + File.separatorChar + "models")
        DirectoryHelper.directoryCreator("output" + File.separatorChar + "Modelib" + File.separatorChar + "assets" + File.separatorChar + "minecraft" + File.separatorChar + "atlases")
        Plugin.instance!!.saveResource(
            "output" + File.separatorChar + "Modelib" + File.separatorChar + "pack.mcmeta",
            true
        )
        Plugin.instance!!.saveResource(
            "output" + File.separatorChar + "Modelib" + File.separatorChar + "pack.png",
            true
        )
        Plugin.instance!!.saveResource(
            "output" + File.separatorChar + "Modelib" + File.separatorChar + "assets" + File.separatorChar + "minecraft" + File.separatorChar + "atlases/blocks.json",
            true
        )
        ZipFile.zip(
            File(Plugin.instance!!.dataFolder.absolutePath + File.separatorChar + "output" + File.separatorChar + "Modelib"),
            Plugin.instance!!.dataFolder.absolutePath + File.separatorChar + "output" + File.separatorChar + "Modelib.zip"
        )
    }
}